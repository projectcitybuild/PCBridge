import { expect, test } from '@plugwright/runner';
import type { PlayerWrapper } from '@plugwright/runner';
import { expectPluginCommandDenied, grantPermission, runPluginCommand } from './helpers/helpers.js';
import type { TestServer } from './helpers/helpers.js';
import { permissionNodes } from './helpers/permission-nodes.generated.js';

async function waitForCondition(
  condition: () => boolean | Promise<boolean>,
  description: string,
  timeoutMs = 10000,
) {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    if (await condition()) return;
    await new Promise(resolve => setTimeout(resolve, 50));
  }
  throw new Error(`Timed out waiting for ${description}`);
}

async function queryTime(server: TestServer, query: 'day' | 'daytime') {
  const output = await server.execute(`minecraft:time query ${query}`);
  const match = output.match(/The time is (-?\d+)/i);
  if (!match) throw new Error(`Could not read ${query} from the server response: ${output}`);
  return Number(match[1]);
}

async function runAndCaptureMessage(
  player: PlayerWrapper,
  command: string,
  expected: RegExp,
) {
  const since = player.getMessageBufferIndex();
  runPluginCommand(player, command);
  await expect(player).toHaveReceivedMessage(expected, { since });
  const message = player.messageBuffer.slice(since).find(value => expected.test(value));
  if (!message) throw new Error(`Expected ${command} to report its current state`);
  return message;
}

test('XP operations update the player total experience', async ({ player, server }) => {
  await expectPluginCommandDenied(player, 'xp set 20');
  await grantPermission(server, player.bot.player.uuid, permissionNodes.gameplay.xp);
  await player.rejoin();

  const operations = [
    { command: 'xp set 20', total: 20 },
    { command: 'xp add 5', total: 25 },
    { command: 'xp subtract 3', total: 22 },
    { command: 'xp reset', total: 0 },
  ];

  for (const operation of operations) {
    runPluginCommand(player, operation.command);
    await waitForCondition(
      () => player.bot.experience.points === operation.total,
      `${player.username}'s total experience to be ${operation.total}`,
    );
  }
});

test('world time commands set the requested time and add a full day', async ({ player, server }) => {
  await expectPluginCommandDenied(player, 'time');
  await grantPermission(server, player.bot.player.uuid, permissionNodes.gameplay.time);
  await player.rejoin();

  try {
    await server.execute('minecraft:gamerule doDaylightCycle false');
    await server.execute('minecraft:time set 1000');

    runPluginCommand(player, 'time set 06:30');
    await waitForCondition(
      async () => (await queryTime(server, 'daytime')) === 500,
      'the world time to be set to 500 ticks',
    );

    const dayBeforeAdd = await queryTime(server, 'day');
    runPluginCommand(player, 'time add 24000');
    await waitForCondition(
      async () => (await queryTime(server, 'day')) === dayBeforeAdd + 1,
      'the world day to advance by one after adding 24000 ticks',
    );

    const worldTime = await queryTime(server, 'daytime');
    const response = await runAndCaptureMessage(player, 'time', /The time in .+ is \d+ ticks/);
    const reportedTime = response.match(/is (\d+) ticks/);
    if (!reportedTime) throw new Error(`Could not read the reported world time: ${response}`);
    expect(Number(reportedTime[1])).toBe(worldTime);
  } finally {
    await server.execute('minecraft:gamerule doDaylightCycle true');
    await server.execute('minecraft:time set day');
  }
});

test('personal time commands change the player clock and reset it to world time', async ({ player, server }) => {
  await expectPluginCommandDenied(player, 'ptime get');
  await grantPermission(server, player.bot.player.uuid, permissionNodes.gameplay.playerTime);
  await player.rejoin();

  try {
    await server.execute('minecraft:gamerule doDaylightCycle false');
    await server.execute('minecraft:time set 1000');

    runPluginCommand(player, 'ptime @noon');
    await waitForCondition(
      () => player.bot.time.timeOfDay === 6000,
      'the player client time to show noon',
    );

    const response = await runAndCaptureMessage(
      player,
      'ptime get',
      /personal time is \d+ ticks \(fixed\)/,
    );
    const reportedTime = response.match(/personal time is (\d+) ticks \(fixed\)/);
    if (!reportedTime) throw new Error(`Could not read the reported personal time: ${response}`);
    expect(Number(reportedTime[1])).toBe(player.bot.time.timeOfDay);

    runPluginCommand(player, 'ptime reset');
    await waitForCondition(
      () => player.bot.time.timeOfDay === 1000,
      'the player client time to return to the world time after reset',
    );
  } finally {
    runPluginCommand(player, 'ptime reset');
    try {
      await waitForCondition(
        () => player.bot.time.timeOfDay === 1000,
        'the player personal time to reset during cleanup',
      );
    } finally {
      await server.execute('minecraft:gamerule doDaylightCycle true');
      await server.execute('minecraft:time set day');
    }
  }
});

test('world weather commands change clear and timed storm states', async ({ player, server }) => {
  await expectPluginCommandDenied(player, 'weather storm');
  await grantPermission(server, player.bot.player.uuid, permissionNodes.gameplay.weather);
  await player.rejoin();

  await server.execute('minecraft:weather clear');
  await waitForCondition(() => !player.bot.isRaining, 'the world weather to be clear before testing');
  try {
    runPluginCommand(player, 'weather storm 30');
    await waitForCondition(() => player.bot.isRaining, 'the world weather to become stormy');

    runPluginCommand(player, 'weather sun');
    await waitForCondition(() => !player.bot.isRaining, 'the world weather to become clear');

    runPluginCommand(player, 'weather storm 2');
    await waitForCondition(() => player.bot.isRaining, 'the timed storm to start');
    await new Promise(resolve => setTimeout(resolve, 500));
    expect(player.bot.isRaining).toBe(true);
    await waitForCondition(() => !player.bot.isRaining, 'the timed storm to expire', 15000);
  } finally {
    await server.execute('minecraft:weather clear');
  }
});

test('personal weather commands override and reset the player weather', async ({ player, server }) => {
  await expectPluginCommandDenied(player, 'pweather get');
  await grantPermission(server, player.bot.player.uuid, permissionNodes.gameplay.playerWeather);
  await player.rejoin();

  await server.execute('minecraft:weather clear');
  await waitForCondition(() => !player.bot.isRaining, 'the world weather to be clear before testing');
  runPluginCommand(player, 'pweather storm');
  await waitForCondition(
    () => player.bot.isRaining,
    'the player client weather to become stormy',
  );

  const response = await runAndCaptureMessage(
    player,
    'pweather get',
    /personal weather is \w+/,
  );
  const reportedWeather = response.match(/personal weather is (\w+)/);
  if (!reportedWeather) throw new Error(`Could not read the reported personal weather: ${response}`);
  expect(reportedWeather[1]).toBe(player.bot.isRaining ? 'downfall' : 'clear');

  runPluginCommand(player, 'pweather reset');
  await runAndCaptureMessage(
    player,
    'pweather get',
    /uses the world's weather/,
  );
});

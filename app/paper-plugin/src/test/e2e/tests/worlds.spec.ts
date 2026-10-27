import { expect, test } from '@plugwright/runner';
import { Vec3 } from 'vec3';
import { expectPluginCommandDenied, grantPermission, runPluginCommand } from './helpers/helpers.js';
import { permissionNodes } from './helpers/permission-nodes.generated.js';

// The rectangle's Z edge is at 8, inside Paper's native square border at 16.
const rectangleBorderCommand = 'worlds border set rectangle 32 16 0 0';
// Border clamp leaves 0.3 blocks for the player's half-width.
const expectedBorderZ = 7.7;

async function runAndExpectMessage(
  player: Parameters<typeof runPluginCommand>[0],
  command: string,
  message: RegExp,
  timeout = 15000,
) {
  const since = player.messageBuffer.length;
  runPluginCommand(player, command);
  await expect(player).toHaveReceivedMessage(message, { since, timeout });
  await new Promise(resolve => setTimeout(resolve, 250));
}

async function clearWorldBorder(player: Parameters<typeof runPluginCommand>[0]) {
  await runAndExpectMessage(player, 'worlds clear', /World border cleared/);
}

test('world borders block movement, teleports, and ender pearls outside the border', async ({ player, server }) => {
  await grantPermission(server, player.bot.player.uuid, permissionNodes.worlds.manage);
  await player.rejoin();

  // Create a runway so movement and the ender pearl reach the border without falling.
  await server.execute('fill -2 79 -12 2 79 12 stone');
  await player.teleport(0, 80, 0);
  await runAndExpectMessage(player, rectangleBorderCommand, /World border set: rectangle/);

  try {
    // Try to walk past the border.
    await player.bot.look(Math.PI, 0, true);
    player.bot.setControlState('forward', true);
    await expect(player).toBeNear(0.5, 80, expectedBorderZ, { tolerance: 0.5, timeout: 10000 });
    player.bot.setControlState('forward', false);

    await player.teleport(0, 80, 0);
    // Vanilla command to trigger a PlayerTeleportEvent.
    await server.execute(`minecraft:tp ${player.username} 0 80 20`);
    await expect(player).toBeNear(0.5, 80, expectedBorderZ, { tolerance: 0.2, timeout: 15000 });

    // Try to throw an ender pearl past the border.
    await player.teleport(0, 80, 0);
    await player.clearInventory();
    await player.giveItem('ender_pearl');
    const pearl = player.bot.inventory.items().find(item => item.name === 'ender_pearl');
    if (!pearl) throw new Error('Expected the ender pearl to be in the player inventory');
    await player.bot.equip(pearl, 'hand');
    await player.bot.look(Math.PI, 0, true);
    player.bot.activateItem();
    await expect(player).toBeNear(0.5, 80, expectedBorderZ, { tolerance: 0.5, timeout: 15000 });
  } finally {
    player.bot.setControlState('forward', false);
    await clearWorldBorder(player);
  }
});

test('world border commands require permission and support setting, viewing, and clearing each shape', async ({ player, server }) => {
  await expectPluginCommandDenied(player, 'worlds border');
  await expectPluginCommandDenied(player, 'worlds border set circle 8');
  await expectPluginCommandDenied(player, 'worlds clear');
  await expectPluginCommandDenied(player, 'worlds border fill');

  await grantPermission(server, player.bot.player.uuid, permissionNodes.worlds.manage);
  await player.rejoin();

  try {
    await runAndExpectMessage(player, 'worlds border set square 8 0 0', /World border set: square/);
    await runAndExpectMessage(player, 'worlds border', /World border: square, center 0\.0, 0\.0; radius 8\.0/);

    await runAndExpectMessage(player, 'worlds border set circle 8 0 0', /World border set: circle/);
    await runAndExpectMessage(player, 'worlds border', /World border: circle, center 0\.0, 0\.0; radius 8\.0/);

    await runAndExpectMessage(player, 'worlds border set rectangle 16 12 0 0', /World border set: rectangle/);
    await runAndExpectMessage(
      player,
      'worlds border',
      /World border: rectangle, center 0\.0, 0\.0; width 16\.0, depth 12\.0/,
    );

    runPluginCommand(player, 'worlds border set rectangle 0 12 0 0');
    await expect(player).toHaveReceivedMessage(/must not be less than/i);
    await runAndExpectMessage(player, 'worlds border', /width 16\.0, depth 12\.0/);

    await clearWorldBorder(player);
    await runAndExpectMessage(player, 'worlds border', /No world border is configured/);

    await player.teleport(0, 80, 0);
    await server.execute(`minecraft:tp ${player.username} 20 80 0`);
    await expect(player).toBeNear(20, 80, 0, { tolerance: 0.5, timeout: 15000 });
  } finally {
    await clearWorldBorder(player);
  }
});

test('circular custom borders reject destinations outside the circle but inside its native square', async ({ player, server }) => {
  await grantPermission(server, player.bot.player.uuid, permissionNodes.worlds.manage);
  await player.rejoin();
  await server.execute('fill -12 79 -12 12 79 12 stone');
  await player.teleport(0, 80, 0);
  await runAndExpectMessage(player, 'worlds border set circle 10 0 0', /World border set: circle/);

  try {
    // (9, 9) is inside Paper's 20-block square border but outside the custom circle.
    await server.execute(`minecraft:tp ${player.username} 9 80 9`);
    await expect(player).toBeNear(6.78, 80, 6.78, { tolerance: 0.7, timeout: 15000 });

    await server.execute(`minecraft:tp ${player.username} 3 80 3`);
    await expect(player).toBeNear(3, 80, 3, { tolerance: 0.5 });
  } finally {
    await clearWorldBorder(player);
  }
});

test('setting a border relocates online players who are already outside it', async ({ player, createPlayer, server }) => {
  const target = await createPlayer();
  await grantPermission(server, player.bot.player.uuid, permissionNodes.worlds.manage);
  await player.rejoin();

  await server.execute('fill -2 79 -2 22 79 2 stone');
  await player.teleport(0, 80, 0);
  await target.teleport(20, 80, 0);
  await runAndExpectMessage(player, 'worlds border set rectangle 16 16 0 0', /World border set: rectangle/);

  try {
    await expect(target).toBeNear(7.7, 80, 0, { tolerance: 0.5, timeout: 15000 });
    await expect(player).toBeNear(0, 80, 0, { tolerance: 0.5 });
  } finally {
    await clearWorldBorder(player);
  }
});

test('players who join or respawn outside a configured border are relocated', async ({ player, createPlayer, server }) => {
  const target = await createPlayer();
  await grantPermission(server, player.bot.player.uuid, permissionNodes.worlds.manage);
  await player.rejoin();

  await server.execute('fill -2 79 -2 22 79 2 stone');
  await player.teleport(0, 80, 0);
  await target.teleport(20, 80, 0);
  const disconnected = new Promise<void>(resolve => target.bot.once('end', () => resolve()));
  target.bot.quit();
  await disconnected;

  try {
    await runAndExpectMessage(player, 'worlds border set rectangle 16 16 0 0', /World border set: rectangle/);
    await target.rejoin();
    await expect(target).toBeNear(7.7, 80, 0, { tolerance: 0.5, timeout: 15000 });

    await server.execute(`minecraft:spawnpoint ${target.username} 20 80 0`);
    await server.execute(`minecraft:kill ${target.username}`);
    await expect(target).toBeNear(7.7, 80, 0, { tolerance: 0.5, timeout: 15000 });
  } finally {
    await server.execute(`minecraft:spawnpoint ${target.username} 0 80 0`);
    await clearWorldBorder(player);
  }
});

test('world borders are isolated between dimensions', async ({ player, server }) => {
  await grantPermission(server, player.bot.player.uuid, permissionNodes.worlds.manage);
  await player.rejoin();
  await player.teleport(0, 80, 0);
  await runAndExpectMessage(player, 'worlds border set rectangle 16 16 0 0', /World border set: rectangle/);

  let inNether = false;
  try {
    await server.execute(`minecraft:execute in minecraft:the_nether run minecraft:tp ${player.username} 0 80 0`);
    inNether = true;
    await runAndExpectMessage(player, 'worlds border', /No world border is configured/);
    await runAndExpectMessage(player, 'worlds border set circle 8 0 0', /World border set: circle/);
    await runAndExpectMessage(player, 'worlds border', /World border: circle/);
    await clearWorldBorder(player);
    inNether = false;

    await server.execute(`minecraft:execute in minecraft:overworld run minecraft:tp ${player.username} 0 80 0`);
    await runAndExpectMessage(player, 'worlds border', /World border: rectangle/);
  } finally {
    if (inNether) await clearWorldBorder(player);
    if (inNether) {
      await server.execute(`minecraft:execute in minecraft:overworld run minecraft:tp ${player.username} 0 80 0`);
    }
    await clearWorldBorder(player);
  }
});

test('non-player teleports and random teleport destinations respect the custom border', async ({ player, server }) => {
  await grantPermission(server, player.bot.player.uuid, permissionNodes.worlds.manage);
  await grantPermission(server, player.bot.player.uuid, permissionNodes.teleportation.random);
  await player.rejoin();
  await server.execute('fill -18 79 -10 18 79 10 stone');
  await player.teleport(0, 80, 0);
  await runAndExpectMessage(player, 'worlds border set rectangle 16 16 0 0', /World border set: rectangle/);

  try {
    await server.execute('minecraft:kill @e[tag=pcbridge_e2e_border]');
    await server.execute('minecraft:summon minecraft:cow 0 80 0 {Tags:["pcbridge_e2e_border"]}');
    await server.execute('minecraft:tp @e[tag=pcbridge_e2e_border,limit=1] 20 80 0');
    await expect(await server.execute('minecraft:data get entity @e[tag=pcbridge_e2e_border,limit=1] Pos'))
      .toMatch(/7\.[0-9]+/);

    await server.execute('minecraft:kill @e[tag=pcbridge_e2e_border]');
    await server.execute('minecraft:summon minecraft:cow 20 80 0 {Tags:["pcbridge_e2e_border"]}');
    const spawnedOutsideBorder = await server.execute(
      'minecraft:execute if entity @e[tag=pcbridge_e2e_border,limit=1] run minecraft:time query daytime',
    );
    expect(spawnedOutsideBorder).not.toMatch(/The time is/);

    const since = player.messageBuffer.length;
    runPluginCommand(player, 'rtp');
    await expect(player).toHaveReceivedMessage(/Teleported to/, { since, timeout: 30000 });
    const { x, z } = player.bot.entity.position;
    expect(Math.abs(x) <= 8 && Math.abs(z) <= 8).toBeTruthy();
  } finally {
    await server.execute('minecraft:kill @e[tag=pcbridge_e2e_border]');
    await clearWorldBorder(player);
  }
});

test('world border fill requires a configured border and preview', async ({ player, server }) => {
  await grantPermission(server, player.bot.player.uuid, permissionNodes.worlds.manage);
  await player.rejoin();

  try {
    await clearWorldBorder(player);
    await runAndExpectMessage(player, 'worlds border fill', /Set a world border before filling chunks/);
    await runAndExpectMessage(player, 'worlds border fill confirm', /There is no active fill preview/);
    await runAndExpectMessage(player, 'worlds border fill status', /There is no world border fill running/);
    await runAndExpectMessage(player, 'worlds border fill cancel', /There is no world border fill running/);
  } finally {
    await clearWorldBorder(player);
  }
});

test('world border fill previews and confirms a small area', async ({ player, server }) => {
  await grantPermission(server, player.bot.player.uuid, permissionNodes.worlds.manage);
  await player.rejoin();

  try {
    await runAndExpectMessage(player, 'worlds border set rectangle 16 16 0 0', /World border set: rectangle/);
    await runAndExpectMessage(player, 'worlds border fill', /Fill preview: up to 4 chunks/);
    await runAndExpectMessage(
      player,
      'worlds border fill confirm',
      /World border fill complete: checked 4\/4 chunks/,
    );
  } finally {
    await clearWorldBorder(player);
  }
});

test('world border fill rejects a preview after the border changes', async ({ player, server }) => {
  await grantPermission(server, player.bot.player.uuid, permissionNodes.worlds.manage);
  await player.rejoin();

  try {
    await runAndExpectMessage(player, 'worlds border set rectangle 16 16 0 0', /World border set: rectangle/);
    await runAndExpectMessage(player, 'worlds border fill', /Fill preview: up to 4 chunks/);
    await runAndExpectMessage(player, 'worlds border set rectangle 32 16 0 0', /World border set: rectangle/);
    await runAndExpectMessage(player, 'worlds border fill confirm', /world border changed/i);
  } finally {
    await clearWorldBorder(player);
  }
});

test('world border fill rejects an area above the chunk limit', async ({ player, server }) => {
  await grantPermission(server, player.bot.player.uuid, permissionNodes.worlds.manage);
  await player.rejoin();

  try {
    await runAndExpectMessage(player, 'worlds border set rectangle 8192 8192 0 0', /World border set: rectangle/);
    await runAndExpectMessage(player, 'worlds border fill', /Fill preview exceeds the limit of 4096 chunks/);
  } finally {
    await clearWorldBorder(player);
  }
});

test('world border fills can be monitored and cancelled', async ({ player, server }) => {
  await grantPermission(server, player.bot.player.uuid, permissionNodes.worlds.manage);
  await player.rejoin();

  try {
    await runAndExpectMessage(player, 'worlds border set circle 256 0 0', /World border set: circle/);
    await runAndExpectMessage(player, 'worlds border fill', /Fill preview: up to \d+ chunks/);
    const fillSince = player.messageBuffer.length;
    runPluginCommand(player, 'worlds border fill confirm');
    await expect(player).toHaveReceivedMessage(/Starting world border fill/, { since: fillSince });
    await runAndExpectMessage(player, 'worlds border fill status', /World border fill running: checked \d+\/\d+ chunks/);
    await runAndExpectMessage(player, 'worlds border fill cancel', /World border fill cancellation requested/);
    await expect(player).toHaveReceivedMessage(/World border fill cancelled: checked \d+\/\d+ chunks/);
  } finally {
    await clearWorldBorder(player);
  }
});

test('world border blocks placing and breaking blocks outside the configured shape', async ({ player, server }) => {
  await grantPermission(server, player.bot.player.uuid, permissionNodes.worlds.manage);
  await player.rejoin();
  await player.setGameMode('creative');
  await server.execute('fill 5 79 -2 10 79 2 stone');
  await player.teleport(6.5, 80, 0);
  await player.bot.waitForChunksToLoad();
  await runAndExpectMessage(player, 'worlds border set rectangle 16 16 0 0', /World border set: rectangle/);

  try {
    const outsideBlock = new Vec3(8, 79, 0);
    const block = player.bot.blockAt(outsideBlock);
    if (!block || block.name !== 'stone') throw new Error('Expected a stone block outside the border');
    await player.bot.dig(block);
    const intactBlock = await server.execute(
      'minecraft:execute if block 8 79 0 minecraft:stone run minecraft:time query daytime',
    );
    expect(intactBlock).toMatch(/The time is/);

    await player.clearInventory();
    await player.giveItem('stone');
    const stone = player.bot.inventory.items().find(item => item.name === 'stone');
    if (!stone) throw new Error('Expected stone in the player inventory');
    await player.bot.equip(stone, 'hand');
    await server.execute('minecraft:setblock 8 80 0 minecraft:air');
    const placementReference = player.bot.blockAt(outsideBlock);
    if (!placementReference) throw new Error('Expected a reference block outside the border');
    await expect(player.bot.placeBlock(placementReference, new Vec3(0, 1, 0)))
      .rejects.toThrow(/Server refused to place/);
    const emptyBlock = await server.execute(
      'minecraft:execute if block 8 80 0 minecraft:air run minecraft:time query daytime',
    );
    expect(emptyBlock).toMatch(/The time is/);
  } finally {
    await clearWorldBorder(player);
    await player.setGameMode('survival');
  }
});

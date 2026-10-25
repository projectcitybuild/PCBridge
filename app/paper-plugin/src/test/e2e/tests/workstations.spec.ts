import { expect, test } from '@plugwright/runner';
import { expectPluginCommandDenied, grantPermission, runPluginCommand } from './helpers/helpers.js';
import { permissionNodes } from './helpers/permission-nodes.generated.js';

const workstationCommands = [
  { command: 'cartographytable', title: /cartography_table/i },
  { command: 'enchanting', title: /enchant/i },
  { command: 'grindstone', title: /grindstone/i },
  { command: 'loom', title: /loom/i },
  { command: 'smithingtable', title: /upgrade/i },
  { command: 'stonecutter', title: /stonecutter/i },
  { command: 'workbench', title: /crafting/i },
] as const;

test('players with permission can open a portable anvil', async ({ player, server }) => {
  await expectPluginCommandDenied(player, 'anvil');
  await grantPermission(server, player.bot.player.uuid, permissionNodes.workstations.use);
  await player.rejoin();

  runPluginCommand(player, 'anvil');

  const anvil = await player.gui({ title: /repair/i });
  expect(anvil.title).toMatch(/repair/i);
});

test('players with permission can open every other portable workstation', async ({ player, server }) => {
  for (const { command } of workstationCommands) {
    await expectPluginCommandDenied(player, command);
  }

  await grantPermission(server, player.bot.player.uuid, permissionNodes.workstations.use);
  await player.rejoin();

  for (const { command, title } of workstationCommands) {
    runPluginCommand(player, command);

    const workstation = await player.gui({ title });
    expect(workstation.title).toMatch(title);
  }
});

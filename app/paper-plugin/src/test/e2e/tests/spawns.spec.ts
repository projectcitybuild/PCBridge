import { expect, test } from '@plugwright/runner';
import { expectPluginCommandDenied, grantPermission, prepareFloor, runPluginCommand } from './helpers/helpers.js';
import { permissionNodes } from './helpers/permission-nodes.generated.js';

test('players can set and return to the server spawn', async ({ player, server }) => {
  await expectPluginCommandDenied(player, 'setspawn');
  await grantPermission(server, player.bot.player.uuid, permissionNodes.spawns.manage);
  await player.rejoin();
  await prepareFloor(server, 20, 20);
  await prepareFloor(server, 100, 100);
  await player.teleport(20, 80, 20);

  runPluginCommand(player, 'setspawn');
  await expect(player).toHaveReceivedMessage(/World spawn set/);

  await player.teleport(100, 80, 100);
  runPluginCommand(player, 'spawn');

  await expect(player).toBeNear(20, 80, 20, { tolerance: 2, timeout: 15000 });
});

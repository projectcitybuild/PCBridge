import { expect, test } from '@plugwright/runner';
import { expectPluginCommandDenied, grantPermission, runPluginCommand } from './helpers/helpers.js';
import { permissionNodes } from './helpers/permission-nodes.generated.js';

test('maintenance mode can be toggled and reports the current state', async ({ player, server }) => {
  await expectPluginCommandDenied(player, 'maintenance off');
  await grantPermission(server, player.bot.player.uuid, permissionNodes.maintenance.manage);
  await player.rejoin();

  try {
    runPluginCommand(player, 'maintenance off');
    await expect(player).toHaveReceivedMessage(/Maintenance mode is (?:now|already) OFF/);

    runPluginCommand(player, 'maintenance on');
    await expect(player).toHaveReceivedMessage('Maintenance mode is now ON');

    runPluginCommand(player, 'maintenance');
    await expect(player).toHaveReceivedMessage('Maintenance mode is currently ON');
  } finally {
    runPluginCommand(player, 'maintenance off');
    await expect(player).toHaveReceivedMessage(/Maintenance mode is (?:now|already) OFF/);
  }
});

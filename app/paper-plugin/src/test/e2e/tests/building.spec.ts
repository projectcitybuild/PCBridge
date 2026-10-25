import { expect, test } from '@plugwright/runner';
import { expectPluginCommandDenied, grantPermission, runPluginCommand } from './helpers/helpers.js';
import { permissionNodes } from './helpers/permission-nodes.generated.js';

test('players with permission can obtain an invisible item frame', async ({ player, server }) => {
  await expectPluginCommandDenied(player, 'invisframe');
  await grantPermission(server, player.bot.player.uuid, permissionNodes.building.invisFrame);
  await player.rejoin();
  await player.clearInventory();

  runPluginCommand(player, 'invisframe');

  await expect(player).toContainItem('item_frame', { timeout: 15000 });
  expect(player.bot.inventory.items().some(item => item.name === 'item_frame')).toBeTruthy();
});

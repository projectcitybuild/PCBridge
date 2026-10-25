import { expect, test } from '@plugwright/runner';
import { expectPluginCommandDenied, grantPermission, runPluginCommand } from './helpers/helpers.js';
import { permissionNodes } from './helpers/permission-nodes.generated.js';

test('moderators can kick a player with a reason', async ({ player, createPlayer, server }) => {
  const target = await createPlayer();
  const disconnected = new Promise<void>(resolve => target.bot.once('end', () => resolve()));

  await expectPluginCommandDenied(player, `kick ${target.username} E2E test`);
  await grantPermission(server, player.bot.player.uuid, permissionNodes.moderate.moderate);
  await player.rejoin();

  runPluginCommand(player, `kick ${target.username} E2E test`);

  await disconnected;
  await expect(player).toHaveReceivedMessage(`${target.username} was kicked: E2E test`);
});

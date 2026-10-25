import { expect, test } from '@plugwright/runner';
import { expectCommandDenied, grantPermission } from './helpers/helpers.js';
import { permissionNodes } from './helpers/permission-nodes.generated.js';

test('non-staff players cannot see messages sent to staff chat', async ({ player, createPlayer, server }) => {
  const sender = await createPlayer();
  const message = `staff-only-${Date.now()}`;

  await expectCommandDenied(sender, `/a ${message}`);

  await grantPermission(server, sender.bot.player.uuid, permissionNodes.staffChat.channel);
  await sender.rejoin();
  sender.chat(`/a ${message}`);

  await expect(sender).toHaveReceivedMessage(`(Staff) ${sender.username}: ${message}`);
  await expect(player).not.toHaveReceivedMessage(message, { timeout: 1000 });
});

import { expect, test } from '@plugwright/runner';

test('non-staff players cannot see messages sent to staff chat', async ({ player, createPlayer, server }) => {
  const sender = await createPlayer();
  const message = `staff-only-${Date.now()}`;

  await server.execute(`lp user ${sender.bot.player.uuid} permission set pcbridge.chat.staff_channel true`);
  await sender.rejoin();
  sender.chat(`/a ${message}`);

  await expect(sender).toHaveReceivedMessage(`(Staff) ${sender.username}: ${message}`);
  await expect(player).not.toHaveReceivedMessage(message, { timeout: 1000 });
});

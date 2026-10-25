import { expect, test } from '@plugwright/runner';

test('players cannot remove operator status with the vanilla deop command', async ({ player }) => {
  await player.makeOp();

  try {
    player.chat(`/deop ${player.username}`);
    player.chat('/gamemode creative');

    await expect(player).toHaveReceivedMessage(/Set own game mode to Creative Mode/);
  } finally {
    await player.deOp();
  }
});

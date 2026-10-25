import { expect, test } from '@plugwright/runner';
import {
  expectCommandDenied,
  expectPluginCommandDenied,
  grantPermission,
  runPluginCommand,
} from './helpers/helpers.js';
import { permissionNodes } from './helpers/permission-nodes.generated.js';

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

test('players cannot use vanilla op to grant themselves or others operator status', async ({ player, createPlayer }) => {
    const target = await createPlayer();
    await player.makeOp();

    try {
      player.chat(`/op ${player.username}`);
      player.chat('/gamemode creative');
      await expect(player).toHaveReceivedMessage(/Set own game mode to Creative Mode/);

      player.chat(`/op ${target.username}`);
      await expectCommandDenied(target, '/gamemode creative');

      player.chat(`/minecraft:op ${target.username}`);
      await expectCommandDenied(target, '/gamemode survival');
    } finally {
      await player.deOp();
      await target.deOp();
    }
});

test('removes stale operator status when a player joins without an active elevation', async ({ player }) => {
  await player.makeOp();

  try {
    player.chat('/gamemode creative');
    await expect(player).toHaveReceivedMessage(/Set own game mode to Creative Mode/);

    await player.rejoin();
    await expectCommandDenied(player, '/gamemode survival');
  } finally {
    await player.deOp();
  }
});

test('PIM elevation status is permission-gated and reports when no elevation is active', async ({ player, server }) => {
  await expectPluginCommandDenied(player, 'pim op status');
  await grantPermission(server, player.bot.player.uuid, permissionNodes.pim.op);
  await player.rejoin();

  runPluginCommand(player, 'pim op status');
  await expect(player).toHaveReceivedMessage('You are not currently OP elevated');
});

import { expect, test } from '@plugwright/runner';
import { expectPluginCommandDenied, grantPermission, prepareFloor, runPluginCommand } from './helpers/helpers.js';
import { permissionNodes } from './helpers/permission-nodes.generated.js';

test('teleport request moves the requester only after acceptance', async ({ player, createPlayer, server }) => {
  runPluginCommand(player, 'tp cancel');
  await expect(player).toHaveReceivedMessage('No matching teleport request was found');

  const target = await createPlayer();
  await expectPluginCommandDenied(player, `tp ${target.username}`);
  await grantPermission(server, player.bot.player.uuid, permissionNodes.teleportation.request);

  await prepareFloor(server, 0, 0);
  await prepareFloor(server, 100, 100);
  await player.teleport(0, 80, 0);
  await target.teleport(100, 80, 100);

  runPluginCommand(player, `tp ${target.username}`);
  await expect(target).toHaveReceivedMessage('wants to teleport to you');
  await expect(player).toBeNear(0, 80, 0, { tolerance: 2 });

  runPluginCommand(target, `tp accept ${player.username}`);
  await expect(target).toHaveReceivedMessage(`Teleport request from ${player.username} accepted`);
  await expect(player).toHaveReceivedMessage(`Teleported to ${target.username}`);
  await expect(player).toBeNear(100, 80, 100, { tolerance: 2, timeout: 15000 });
});

test('denying a teleport request leaves the requester in place', async ({ player, createPlayer, server }) => {
  const target = await createPlayer();
  await expectPluginCommandDenied(player, `tp ${target.username}`);
  await grantPermission(server, player.bot.player.uuid, permissionNodes.teleportation.request);

  await prepareFloor(server, 0, 0);
  await prepareFloor(server, 100, 100);
  await player.teleport(0, 80, 0);
  await target.teleport(100, 80, 100);

  runPluginCommand(player, `tp ${target.username}`);
  await expect(target).toHaveReceivedMessage('wants to teleport to you');

  runPluginCommand(target, `tp deny ${player.username}`);
  await expect(target).toHaveReceivedMessage(`Teleport request from ${player.username} denied`);
  await expect(player).toHaveReceivedMessage(`${target.username} denied your teleport request`);
  await expect(player).toBeNear(0, 80, 0, { tolerance: 2 });
});

test('requesters can cancel an outgoing teleport request', async ({ player, createPlayer, server }) => {
  const target = await createPlayer();
  await expectPluginCommandDenied(player, `tp ${target.username}`);
  await grantPermission(server, player.bot.player.uuid, permissionNodes.teleportation.request);

  await prepareFloor(server, 0, 0);
  await prepareFloor(server, 100, 100);
  await player.teleport(0, 80, 0);
  await target.teleport(100, 80, 100);

  runPluginCommand(player, `tp ${target.username}`);
  await expect(target).toHaveReceivedMessage('wants to teleport to you');

  runPluginCommand(player, 'tp cancel');
  await expect(player).toHaveReceivedMessage(`Teleport request to ${target.username} cancelled`);
  await expect(target).toHaveReceivedMessage(`${player.username} cancelled their teleport request`);

  runPluginCommand(target, `tp accept ${player.username}`);
  await expect(target).toHaveReceivedMessage('No matching teleport request was found');
  await expect(player).toBeNear(0, 80, 0, { tolerance: 2 });
});

test('here requests move the target only after acceptance', async ({ player, createPlayer, server }) => {
  const target = await createPlayer();
  await expectPluginCommandDenied(player, `tp here ${target.username}`);
  await grantPermission(server, player.bot.player.uuid, permissionNodes.teleportation.others);

  await prepareFloor(server, 0, 0);
  await prepareFloor(server, 100, 100);
  await player.teleport(0, 80, 0);
  await target.teleport(100, 80, 100);

  runPluginCommand(player, `tp here ${target.username}`);
  await expect(target).toHaveReceivedMessage('wants you to teleport to them');
  await expect(target).toBeNear(100, 80, 100, { tolerance: 2 });

  runPluginCommand(target, `tp accept ${player.username}`);
  await expect(target).toBeNear(0, 80, 0, { tolerance: 2, timeout: 15000 });
  await expect(target).toHaveReceivedMessage(`Teleport request from ${player.username} accepted`);
  await expect(player).toHaveReceivedMessage(`Teleported ${target.username} to ${player.username}`);
});

test('force teleports directly without a consent request', async ({ player, createPlayer, server }) => {
  const target = await createPlayer();
  await expectPluginCommandDenied(player, `tp to ${target.username}`);
  await grantPermission(server, player.bot.player.uuid, permissionNodes.teleportation.request);
  await player.rejoin();
  await expectPluginCommandDenied(player, `tp to ${target.username} --force`);
  await grantPermission(server, player.bot.player.uuid, permissionNodes.teleportation.force);
  await player.rejoin();

  await prepareFloor(server, 0, 0);
  await prepareFloor(server, 100, 100);
  await player.teleport(0, 80, 0);
  await target.teleport(100, 80, 100);

  runPluginCommand(player, `tp to ${target.username} --force`);

  await expect(player).toHaveReceivedMessage(`Teleported to ${target.username}`);
  await expect(target).toHaveReceivedMessage(`${player.username} teleported to you`);
  await expect(player).toBeNear(100, 80, 100, { tolerance: 2, timeout: 15000 });
});

test('position teleports to the requested coordinates', async ({ player, server }) => {
  await expectPluginCommandDenied(player, 'tp pos 200 80 200');
  await grantPermission(server, player.bot.player.uuid, permissionNodes.teleportation.position);
  await prepareFloor(server, 200, 200);
  await player.teleport(0, 80, 0);

  runPluginCommand(player, 'tp pos 200 80 200');

  await expect(player).toBeNear(200, 80, 200, { tolerance: 2, timeout: 15000 });
});

test('back toggles between the two most recent teleport locations', async ({ player, server }) => {
  await expectPluginCommandDenied(player, 'tp back');
  await expectPluginCommandDenied(player, 'tp pos 100 80 100');
  await grantPermission(server, player.bot.player.uuid, permissionNodes.teleportation.position);
  await grantPermission(server, player.bot.player.uuid, permissionNodes.teleportation.back);
  await prepareFloor(server, 0, 0);
  await prepareFloor(server, 100, 100);
  await prepareFloor(server, 200, 200);
  await player.teleport(0, 80, 0);

  runPluginCommand(player, 'tp pos 100 80 100');
  await expect(player).toBeNear(100, 80, 100, { tolerance: 2, timeout: 15000 });
  runPluginCommand(player, 'tp pos 200 80 200');
  await expect(player).toBeNear(200, 80, 200, { tolerance: 2, timeout: 15000 });

  runPluginCommand(player, 'tp back');
  await expect(player).toBeNear(100, 80, 100, { tolerance: 2, timeout: 15000 });
  runPluginCommand(player, 'tp back');
  await expect(player).toBeNear(200, 80, 200, { tolerance: 2, timeout: 15000 });
});

test('send creates a consent request for one player to teleport to another', async ({ player, createPlayer, server }) => {
  const requester = await createPlayer();
  const destination = await createPlayer();
  await expectPluginCommandDenied(player, `tp send ${requester.username} ${destination.username}`);
  await grantPermission(server, player.bot.player.uuid, permissionNodes.teleportation.others);

  await prepareFloor(server, 0, 0);
  await prepareFloor(server, 100, 100);
  await prepareFloor(server, 200, 200);
  await player.teleport(0, 80, 0);
  await requester.teleport(100, 80, 100);
  await destination.teleport(200, 80, 200);

  runPluginCommand(player, `tp send ${requester.username} ${destination.username}`);
  await expect(requester).toHaveReceivedMessage('wants you to teleport to them');
  runPluginCommand(requester, `tp accept ${destination.username}`);

  await expect(requester).toBeNear(200, 80, 200, { tolerance: 2, timeout: 15000 });
});

test('all force teleports online players to the selected target', async ({ player, createPlayer, server }) => {
  const target = await createPlayer();
  await expectPluginCommandDenied(player, `tp all ${target.username} --force`);
  await grantPermission(server, player.bot.player.uuid, permissionNodes.teleportation.all);
  await grantPermission(server, player.bot.player.uuid, permissionNodes.teleportation.force);
  const otherPlayer = await createPlayer();

  await prepareFloor(server, 0, 0);
  await prepareFloor(server, 100, 100);
  await prepareFloor(server, 200, 200);
  await player.teleport(0, 80, 0);
  await target.teleport(100, 80, 100);
  await otherPlayer.teleport(200, 80, 200);

  runPluginCommand(player, `tp all ${target.username} --force`);

  await expect(player).toBeNear(100, 80, 100, { tolerance: 2, timeout: 15000 });
  await expect(otherPlayer).toBeNear(100, 80, 100, { tolerance: 2, timeout: 15000 });
});

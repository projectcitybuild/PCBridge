import { expect, test } from '@plugwright/runner';

type TestServer = {
  execute(command: string): Promise<string>;
};

async function grantPermission(server: TestServer, uuid: string, permission: string) {
  await server.execute(`lp user ${uuid} permission set ${permission} true`);
}

async function prepareFloor(server: TestServer, x: number, z: number) {
  await server.execute(`fill ${x - 2} 79 ${z - 2} ${x + 2} 79 ${z + 2} stone`);
}

test('teleport request moves the requester only after acceptance', async ({ player, createPlayer, server }) => {
  player.chat('/pcbridge:tp cancel');
  await expect(player).toHaveReceivedMessage('No matching teleport request was found');

  await grantPermission(server, player.bot.player.uuid, 'pcbridge.teleport.request');
  const target = await createPlayer();

  await prepareFloor(server, 0, 0);
  await prepareFloor(server, 100, 100);
  await player.teleport(0, 80, 0);
  await target.teleport(100, 80, 100);

  player.chat(`/pcbridge:tp ${target.username}`);
  await expect(target).toHaveReceivedMessage('wants to teleport to you');
  await expect(player).toBeNear(0, 80, 0, { tolerance: 2 });

  target.chat(`/pcbridge:tp accept ${player.username}`);
  await expect(target).toHaveReceivedMessage(`Teleport request from ${player.username} accepted`);
  await expect(player).toHaveReceivedMessage(`Teleported to ${target.username}`);
  await expect(player).toBeNear(100, 80, 100, { tolerance: 2, timeout: 15000 });
});

test('denying a teleport request leaves the requester in place', async ({ player, createPlayer, server }) => {
  await grantPermission(server, player.bot.player.uuid, 'pcbridge.teleport.request');
  const target = await createPlayer();

  await prepareFloor(server, 0, 0);
  await prepareFloor(server, 100, 100);
  await player.teleport(0, 80, 0);
  await target.teleport(100, 80, 100);

  player.chat(`/pcbridge:tp ${target.username}`);
  await expect(target).toHaveReceivedMessage('wants to teleport to you');

  target.chat(`/pcbridge:tp deny ${player.username}`);
  await expect(target).toHaveReceivedMessage(`Teleport request from ${player.username} denied`);
  await expect(player).toHaveReceivedMessage(`${target.username} denied your teleport request`);
  await expect(player).toBeNear(0, 80, 0, { tolerance: 2 });
});

test('requesters can cancel an outgoing teleport request', async ({ player, createPlayer, server }) => {
  await grantPermission(server, player.bot.player.uuid, 'pcbridge.teleport.request');
  const target = await createPlayer();

  await prepareFloor(server, 0, 0);
  await prepareFloor(server, 100, 100);
  await player.teleport(0, 80, 0);
  await target.teleport(100, 80, 100);

  player.chat(`/pcbridge:tp ${target.username}`);
  await expect(target).toHaveReceivedMessage('wants to teleport to you');

  player.chat('/pcbridge:tp cancel');
  await expect(player).toHaveReceivedMessage(`Teleport request to ${target.username} cancelled`);
  await expect(target).toHaveReceivedMessage(`${player.username} cancelled their teleport request`);

  target.chat(`/pcbridge:tp accept ${player.username}`);
  await expect(target).toHaveReceivedMessage('No matching teleport request was found');
  await expect(player).toBeNear(0, 80, 0, { tolerance: 2 });
});

test('here requests move the target only after acceptance', async ({ player, createPlayer, server }) => {
  await grantPermission(server, player.bot.player.uuid, 'pcbridge.teleport.others');
  const target = await createPlayer();

  await prepareFloor(server, 0, 0);
  await prepareFloor(server, 100, 100);
  await player.teleport(0, 80, 0);
  await target.teleport(100, 80, 100);

  player.chat(`/pcbridge:tp here ${target.username}`);
  await expect(target).toHaveReceivedMessage('wants you to teleport to them');
  await expect(target).toBeNear(100, 80, 100, { tolerance: 2 });

  target.chat(`/pcbridge:tp accept ${player.username}`);
  await expect(target).toBeNear(0, 80, 0, { tolerance: 2, timeout: 15000 });
  await expect(target).toHaveReceivedMessage(`Teleport request from ${player.username} accepted`);
  await expect(player).toHaveReceivedMessage(`Teleported ${target.username} to ${player.username}`);
});

test('force teleports directly without a consent request', async ({ player, createPlayer, server }) => {
  await grantPermission(server, player.bot.player.uuid, 'pcbridge.teleport.request');
  await grantPermission(server, player.bot.player.uuid, 'pcbridge.teleport.force');
  const target = await createPlayer();

  await prepareFloor(server, 0, 0);
  await prepareFloor(server, 100, 100);
  await player.teleport(0, 80, 0);
  await target.teleport(100, 80, 100);

  player.chat(`/pcbridge:tp to ${target.username} --force`);

  await expect(player).toHaveReceivedMessage(`Teleported to ${target.username}`);
  await expect(target).toHaveReceivedMessage(`${player.username} teleported to you`);
  await expect(player).toBeNear(100, 80, 100, { tolerance: 2, timeout: 15000 });
});

test('position teleports to the requested coordinates', async ({ player, server }) => {
  await grantPermission(server, player.bot.player.uuid, 'pcbridge.teleport.position');
  await prepareFloor(server, 200, 200);
  await player.teleport(0, 80, 0);

  player.chat('/pcbridge:tp pos 200 80 200');

  await expect(player).toBeNear(200, 80, 200, { tolerance: 2, timeout: 15000 });
});

test('back toggles between the two most recent teleport locations', async ({ player, server }) => {
  await grantPermission(server, player.bot.player.uuid, 'pcbridge.teleport.position');
  await grantPermission(server, player.bot.player.uuid, 'pcbridge.teleport.back');
  await prepareFloor(server, 0, 0);
  await prepareFloor(server, 100, 100);
  await prepareFloor(server, 200, 200);
  await player.teleport(0, 80, 0);

  player.chat('/pcbridge:tp pos 100 80 100');
  await expect(player).toBeNear(100, 80, 100, { tolerance: 2, timeout: 15000 });
  player.chat('/pcbridge:tp pos 200 80 200');
  await expect(player).toBeNear(200, 80, 200, { tolerance: 2, timeout: 15000 });

  player.chat('/pcbridge:tp back');
  await expect(player).toBeNear(100, 80, 100, { tolerance: 2, timeout: 15000 });
  player.chat('/pcbridge:tp back');
  await expect(player).toBeNear(200, 80, 200, { tolerance: 2, timeout: 15000 });
});

test('send creates a consent request for one player to teleport to another', async ({ player, createPlayer, server }) => {
  await grantPermission(server, player.bot.player.uuid, 'pcbridge.teleport.others');
  const requester = await createPlayer();
  const destination = await createPlayer();

  await prepareFloor(server, 0, 0);
  await prepareFloor(server, 100, 100);
  await prepareFloor(server, 200, 200);
  await player.teleport(0, 80, 0);
  await requester.teleport(100, 80, 100);
  await destination.teleport(200, 80, 200);

  player.chat(`/pcbridge:tp send ${requester.username} ${destination.username}`);
  await expect(requester).toHaveReceivedMessage('wants you to teleport to them');
  requester.chat(`/pcbridge:tp accept ${destination.username}`);

  await expect(requester).toBeNear(200, 80, 200, { tolerance: 2, timeout: 15000 });
});

test('all force teleports online players to the selected target', async ({ player, createPlayer, server }) => {
  await grantPermission(server, player.bot.player.uuid, 'pcbridge.teleport.all');
  await grantPermission(server, player.bot.player.uuid, 'pcbridge.teleport.force');
  const target = await createPlayer();
  const otherPlayer = await createPlayer();

  await prepareFloor(server, 0, 0);
  await prepareFloor(server, 100, 100);
  await prepareFloor(server, 200, 200);
  await player.teleport(0, 80, 0);
  await target.teleport(100, 80, 100);
  await otherPlayer.teleport(200, 80, 200);

  player.chat(`/pcbridge:tp all ${target.username} --force`);

  await expect(player).toBeNear(100, 80, 100, { tolerance: 2, timeout: 15000 });
  await expect(otherPlayer).toBeNear(100, 80, 100, { tolerance: 2, timeout: 15000 });
});

import { expect, test } from '@plugwright/runner';
import { grantPermission, runPluginCommand } from './helpers/helpers.js';
import { permissionNodes } from './helpers/permission-nodes.generated.js';

// The rectangle's Z edge is at 8, inside Paper's native square border at 16.
const rectangleBorderCommand = 'worlds border set rectangle 32 16 0 0';
// Border clamp leaves 0.3 blocks for the player's half-width.
const expectedBorderZ = 7.7;

test('world borders block movement, teleports, and ender pearls outside the border', async ({ player, server }) => {
  await grantPermission(server, player.bot.player.uuid, permissionNodes.worlds.manage);
  await player.rejoin();

  const runAndExpectMessage = async (command: string, message: RegExp, timeout = 15000) => {
    const since = player.messageBuffer.length;
    runPluginCommand(player, command);
    await expect(player).toHaveReceivedMessage(message, { since, timeout });
  };

  // Create a runway so movement and the ender pearl reach the border without falling.
  await server.execute('fill -2 79 -12 2 79 12 stone');
  await player.teleport(0, 80, 0);
  await runAndExpectMessage(rectangleBorderCommand, /World border set: rectangle/);

  // Try walk past the border
  await player.bot.look(Math.PI, 0, true);
  player.bot.setControlState('forward', true);
  await expect(player).toBeNear(0.5, 80, expectedBorderZ, { tolerance: 0.5, timeout: 10000 });
  player.bot.setControlState('forward', false);

  await player.teleport(0, 80, 0);
  // Vanilla command to trigger a PlayerTeleportEvent
  await server.execute(`minecraft:tp ${player.username} 0 80 20`);
  await expect(player).toBeNear(0.5, 80, expectedBorderZ, { tolerance: 0.2, timeout: 15000 });

  // Try throw an ender pearl past the border
  await player.teleport(0, 80, 0);
  await player.clearInventory();
  await player.giveItem('ender_pearl');
  const pearl = player.bot.inventory.items().find(item => item.name === 'ender_pearl');
  if (!pearl) throw new Error('Expected the ender pearl to be in the player inventory');
  await player.bot.equip(pearl, 'hand');
  await player.bot.look(Math.PI, 0, true);
  player.bot.activateItem();
  await expect(player).toBeNear(0.5, 80, expectedBorderZ, { tolerance: 0.5, timeout: 15000 });
});

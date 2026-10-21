import { definePlugin, type TestContext } from '@plugwright/runner';

const playersByTest = new WeakMap<TestContext, Set<string>>();

export default definePlugin({
  name: 'reset-player-permissions',

  extendContext(context) {
    const playerUuids = new Set([context.player.bot.player.uuid]);
    playersByTest.set(context, playerUuids);

    const createPlayer = context.createPlayer;
    return {
      createPlayer: async (options?: Parameters<TestContext['createPlayer']>[0]) => {
        const player = await createPlayer(options);
        playerUuids.add(player.bot.player.uuid);
        return player;
      },
    };
  },

  async afterEach(context) {
    const playerUuids = playersByTest.get(context);
    if (!playerUuids) return;

    playersByTest.delete(context);
    for (const uuid of playerUuids) {
      await context.server.execute(`lp user ${uuid} permission clear`);
    }
  },
});

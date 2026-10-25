import { expect } from '@plugwright/runner';
import type { PlayerWrapper } from '@plugwright/runner';

export type TestServer = {
  execute(command: string): Promise<string>;
};

export async function grantPermission(server: TestServer, uuid: string, permission: string) {
  await server.execute(`lp user ${uuid} permission set ${permission} true`);
}

export function runPluginCommand(player: PlayerWrapper, command: string) {
  player.chat(`/pcbridge:${command}`);
}

export async function expectCommandDenied(player: PlayerWrapper, command: string) {
  const since = player.messageBuffer.length;
  player.chat(command);
  await expect(player).toHaveReceivedMessage(
    /(?:Unknown or incomplete|Incorrect argument for) command|You do not have permission/i,
    { since },
  );
}

export async function expectPluginCommandDenied(player: PlayerWrapper, command: string) {
  await expectCommandDenied(player, `/pcbridge:${command}`);
}

export async function prepareFloor(server: TestServer, x: number, z: number) {
  await server.execute(`fill ${x - 2} 79 ${z - 2} ${x + 2} 79 ${z + 2} stone`);
}

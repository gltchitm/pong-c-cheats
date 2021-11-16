import { writable } from 'svelte/store'

namespace ServerPackets {
    export enum ServerboundPackets {
        Hello,
        Authenticate,
        Forward
    }
    export enum ClientboundPackets {
        Ok,
        NeverSaidHello,
        AlreadySaidHello,
        Busy,
        NotAutheticated,
        TooManyAttempts,
        IncorrectToken,
        AlreadyAuthenticated,
        Forward
    }
    export interface ClientboundPacket {
        id: ClientboundPackets,
        forward: string
    }
    export interface ServerboundPacket {
        id: ServerboundPackets,
        token: string,
        forward: string
    }
}
namespace DaemonPackets {
    export enum ServerboundPackets {
        Attach,
        ChangeLeftScore,
        ChangeRightScore,
        GetLeftScore,
        GetRightScore,
        Detach,
        DetachAndExit,
    }
    export enum ClientboundPackets {
        Ok,
        LeftScore,
        RightScore,
        Busy,
        AlreadyAttached,
        NotAttached,
        NotFound,
        MalformedPacket
    }
    export interface ClientboundPacket {
        id: ClientboundPackets,
        score: number
    }
    export interface ServerboundPacket {
        id: ServerboundPackets,
        score: number
    }
}
class Communicator extends WebSocket {
    constructor() {
        super('ws://localhost:13948/ws')
    }

    public waitForOpen(): Promise<void> {
        return new Promise((res, rej) => {
            this.addEventListener('open', () => {
                res()
            }, { once: true })
            this.addEventListener('error', () => {
                rej()
            }, { once: true })
        })
    }
    public hello() {
        return this.communicateWithServer(ServerPackets.ServerboundPackets.Hello, null, null)
    }
    public authenticate(token: string) {
        return this.communicateWithServer(ServerPackets.ServerboundPackets.Authenticate, token, null)
    }
    public attach() {
        return this.communicateWithDaemon(DaemonPackets.ServerboundPackets.Attach, null)
    }
    public detach() {
        return this.communicateWithDaemon(DaemonPackets.ServerboundPackets.Detach, null)
    }
    public getLeftScore() {
        return this.communicateWithDaemon(DaemonPackets.ServerboundPackets.GetLeftScore, null)
    }
    public getRightScore() {
        return this.communicateWithDaemon(DaemonPackets.ServerboundPackets.GetRightScore, null)
    }
    public changeLeftScore(score: number) {
        return this.communicateWithDaemon(DaemonPackets.ServerboundPackets.ChangeLeftScore, score)
    }
    public changeRightScore(score: number) {
        return this.communicateWithDaemon(DaemonPackets.ServerboundPackets.ChangeRightScore, score)
    }
    private communicateWithServer(
        id: ServerPackets.ServerboundPackets,
        token: string,
        forward: string
    ): Promise<ServerPackets.ClientboundPacket> {
        return new Promise(res => {
            this.addEventListener('message', ({ data }) => {
                res(JSON.parse(data))
            }, { once: true })

            this.send(JSON.stringify({ id, token, forward }))
        })
    }
    private async communicateWithDaemon(
        id: DaemonPackets.ServerboundPackets,
        score: number
    ): Promise<DaemonPackets.ClientboundPacket> {
        return JSON.parse(
            (
                await this.communicateWithServer(
                    ServerPackets.ServerboundPackets.Forward,
                    null,
                    JSON.stringify({ id, score }) + '\n'
                )
            ).forward
        )
    }
}

const communicator = writable(null)

export { ServerPackets, DaemonPackets, Communicator, communicator }

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package gcewing.sg.features.network;

import gcewing.sg.features.pdd.network.PddMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * {@link SGNetwork} is a wrapper around {@link SimpleNetworkWrapper} in order to ease the handling of discriminators.
 *
 * @author Ordinastie
 */
public class SGNetwork extends SimpleNetworkWrapper {

    public static final SGNetwork INSTANCE = new SGNetwork("sgcraft2"); //channel "sgcraft" already registered
    /** The global discriminator for each packet. */
    private int discriminator = 0;
    /** Name of the channel used **/
    protected String name;

    /**
     * Instantiates a new {@link SGNetwork}.
     *
     * @param channelName the channel name
     */
    public SGNetwork(String channelName) {
        super(channelName);
        name = channelName;
    }


    /**
     * Register a message with the next discriminator available.
     *
     * @param <REQ> the generic type
     * @param <REPLY> the generic type
     * @param messageHandler the message handler
     * @param requestMessageType the request message type
     * @param side the side
     */
    public <REQ extends IMessage, REPLY extends IMessage> void registerMessage(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler,
            Class<REQ> requestMessageType, Side side) {
        super.registerMessage(messageHandler, requestMessageType, discriminator++, side);
    }

    /**
     * Register a message with the next discriminator available.
     *
     * @param <REQ> the generic type
     * @param <REPLY> the generic type
     * @param messageHandler the message handler
     * @param requestMessageType the request message type
     * @param side the side
     */
    public <REQ extends IMessage, REPLY extends IMessage> void registerMessage(IMessageHandler<? super REQ, ? extends REPLY> messageHandler,
            Class<REQ> requestMessageType, Side side) {
        super.registerMessage(messageHandler, requestMessageType, discriminator++, side);
    }

    public static void init() {
        new PddMessage();
    }
}
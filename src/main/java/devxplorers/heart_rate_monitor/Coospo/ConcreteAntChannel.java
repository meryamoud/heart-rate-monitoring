package devxplorers.heart_rate_monitor.Coospo;

import be.glever.ant.channel.AntChannel;
import be.glever.ant.channel.AntChannelNetwork;
import be.glever.ant.message.AntMessage;
import reactor.core.publisher.Flux;

public class ConcreteAntChannel extends AntChannel {

    private boolean isOpen = false;

    // Constructor accepting a channel number
    public ConcreteAntChannel(byte channelNumber) {
        super();
        setChannelNumber(channelNumber);
    }

    @Override
    public void subscribeTo(Flux<AntMessage> flux) {
        // Example subscription: simply print each received message.
        flux.subscribe(message -> System.out.println("Received message: " + message));
    }

    @Override
    public Flux<AntMessage> getEvents() {
        // Return an empty Flux (replace with actual event stream if needed)
        return Flux.empty();
    }

    // Optional: method to open the channel (for local setup/logging)
    public void open() {
        if (!isOpen) {
            System.out.println("Opening channel...");
            isOpen = true;
            System.out.println("Channel opened successfully.");
        } else {
            System.out.println("Channel is already open.");
        }
    }

    // Optional: method to close the channel
    public void close() {
        if (isOpen) {
            System.out.println("Closing channel...");
            isOpen = false;
            System.out.println("Channel closed.");
        } else {
            System.out.println("Channel is already closed.");
        }
    }

    // Method to assign channel type and network
    public void assign(be.glever.ant.constants.AntChannelType channelType, AntChannelNetwork network) {
        setChannelType(channelType);
        setNetwork(network);
        System.out.println("Channel assigned to type: " + channelType + " with network: " + network);
    }
}

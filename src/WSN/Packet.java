package WSN;

/**
 * Created by Gianluca on 17/07/2017.
 */
public class Packet {

    private Node source, destination;
    private int txAttempts;

    public Packet(Node source, Node destination){
        this.source = source;
        this.destination = destination;
        this.txAttempts = 0;
    }

    public Node getSource(){
        return this.source;
    }
    public Node getDestination(){
        return this.destination;
    }

    public int getTxAttempts(){
        return this.txAttempts;
    }

    public void incrementTxAttempts(){
        this.txAttempts++;
    }
}

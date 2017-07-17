package WSN;

/**
 * Created by Gianluca on 17/07/2017.
 */
public class Packet {

    private Node source, destination;

    public Packet(Node source, Node destination){
        this.source = source;
        this.destination = destination;
    }

    public Node getSource(){
        return this.source;
    }

    public Node getDestination(){
        return this.destination;
    }
}

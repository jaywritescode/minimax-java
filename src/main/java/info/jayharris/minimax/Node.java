package info.jayharris.minimax;

import java.util.Comparator;
import java.util.Set;
import java.util.function.DoubleSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Node<S extends State<S, A>, A extends Action<S, A>> {

    private final S state;

    private final A action;

    private final int depth;

    private DoubleSupplier valueSupplier;

    static final Comparator<Node> comparator = Comparator.comparingDouble(Node::getHeuristicValue);

    private Node(S state, A action, int depth) {
        this.state = state;
        this.action = action;
        this.depth = depth;
        this.valueSupplier = state::eval;
    }

    public Stream<Node<S, A>> successors() {
        return state.actions().stream().map(this::apply);
    }

    private Node<S, A> apply(A successorAction) {
        return new Node<>(successorAction.apply(state), successorAction, depth + 1);
    }

    public S getState() {
        return state;
    }

    public A getAction() {
        return action;
    }

    public int getDepth() {
        return depth;
    }

    void calculateHeuristicValue() {
        setHeuristicValueToConstant(state.eval());
    }

    double getHeuristicValue() {
        return valueSupplier.getAsDouble();
    }

    void setHeuristicValueToConstant(double value) {
        this.valueSupplier = () -> value;
    }

    boolean terminalTest() {
        return state.terminalTest();
    }

    public static <S extends State<S, A>, A extends Action<S, A>> Node<S, A> root(S state) {
        return new Node<>(state, null, 0);
    }
}

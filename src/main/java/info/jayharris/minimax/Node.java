package info.jayharris.minimax;

import java.util.Comparator;
import java.util.List;
import java.util.OptionalLong;
import java.util.stream.Collectors;

class Node<S extends State<S, A>, A extends Action<S, A>> {

    /**
     * A state in the game.
     */
    private final S state;

    /**
     * The action such that {@code action.apply(state.predecessor()).equals(state)}.
     */
    private final A action;

    private final int depth;

    private final NodeFactory<S, A> nodeFactory;

    private OptionalLong utility;

    static Comparator<Node> comparator = Comparator.comparingLong(Node::getUtility);

    Node(S state, A action, int depth) {
        this.state = state;
        this.action = action;
        this.depth = depth;
        this.nodeFactory = new NodeFactory<>(state);
        this.utility = OptionalLong.empty();
    }

    List<Node<S, A>> successors() {
        return state.actions().stream()
                .map(action -> nodeFactory.withAction(action, depth + 1))
                .collect(Collectors.toList());
    }

    boolean terminalTest() {
        return state.terminalTest();
    }

    S getState() {
        return state;
    }

    A getAction() {
        return action;
    }

    int getDepth() {
        return depth;
    }

    long getUtility() {
        return utility.getAsLong();
    }

    void setUtility(long utility) {
        setUtility(OptionalLong.of(utility));
    }

    void setUtility(OptionalLong utility) {
        this.utility = utility;
    }
}

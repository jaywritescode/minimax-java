package info.jayharris.minimax;

import info.jayharris.minimax.transposition.Transpositions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DecisionTreeTest {

    @Test
    @DisplayName("#perform")
    void perform() {
        TestState A, B, C, D, b1, b2, b3, c1, c2, c3, d1, d2, d3;

        b1 = TestState.terminalState("b1", 3);
        b2 = TestState.terminalState("b2", 12);
        b3 = TestState.terminalState("b3", 8);

        c1 = TestState.terminalState("c1", 2);
        c2 = TestState.terminalState("c2", 4);
        c3 = TestState.terminalState("c3", 6);

        d1 = TestState.terminalState("d1", 14);
        d2 = TestState.terminalState("d2", 5);
        d3 = TestState.terminalState("d3", 2);

        B = TestState.nonTerminalState("B", Arrays.asList(
                new TestAction(b1),
                new TestAction(b2),
                new TestAction(b3)
        ));
        C = TestState.nonTerminalState("C", Arrays.asList(
                new TestAction(c1),
                new TestAction(c2),
                new TestAction(c3)
        ));
        D = TestState.nonTerminalState("D", Arrays.asList(
                new TestAction(d1),
                new TestAction(d2),
                new TestAction(d3)
        ));

        A = TestState.nonTerminalState("A", Arrays.asList(
                new TestAction(B),
                new TestAction(C),
                new TestAction(D)
        ));

        DecisionTree<TestState, TestAction> tree = new DecisionTree<>(
                A, new TranspositionAdapter(), new TestHeuristicEvaluationFunction(), new CutoffTestAdapter());

        assertThat(tree.perform()).extracting("successor").first().isSameAs(B);
    }

    @Test
    @DisplayName("it only calculates each state's heuristic value once and only once")
    void calculateHeuristicValueOnlyOnce() {
        Transpositions<TestState> transpositions = new Transpositions<TestState>() {
            final Map<TestState, Double> map = new HashMap<>();

            @Override
            public OptionalDouble get(TestState equivalence) {
                if (map.containsKey(equivalence)) {
                    return OptionalDouble.of(map.get(equivalence));
                }

                return OptionalDouble.empty();
            }

            @Override
            public void put(TestState equivalences, double utility) {
                map.put(equivalences, utility);
            }
        };

        TestState A, B, C, s1, s2, s3, s4;
        TestHeuristicEvaluationFunction fn;

        s1 = TestState.terminalState("s1", 4.0);
        s2 = TestState.terminalState("s2", 5.0);
        s3 = TestState.terminalState("s3", 6.0);
        s4 = TestState.terminalState("s4", 7.0);

        B = TestState.nonTerminalState("B", Arrays.asList(
                new TestAction(s1),
                new TestAction(s2),
                new TestAction(s3)
        ));
        C = TestState.nonTerminalState("C", Arrays.asList(
                new TestAction(s1),
                new TestAction(s2),
                new TestAction(s3),
                new TestAction(s4)
        ));

        A = TestState.nonTerminalState("A", Arrays.asList(
                new TestAction(B),
                new TestAction(C)
        ));

        DecisionTree<TestState, TestAction> tree = new DecisionTree<>(
                A, transpositions, fn = new TestHeuristicEvaluationFunction(), new CutoffTestAdapter());

        tree.perform();

        SoftAssertions softly = new SoftAssertions();
        Stream.of(s1, s2, s3, s4).forEach(state -> softly.assertThat(fn.count(state)).isEqualTo(1));
        softly.assertAll();
    }

    @Test
    @DisplayName("cutoff test obtains")
    void cutoffTestObtains() {
        TestState A, B, C, c1, s1;
        TestHeuristicEvaluationFunction fn;

        s1 = TestState.terminalState("s1", 6);

        c1 = TestState.cutoffTestState("c1", Collections.singletonList(new TestAction(s1)), 7);

        C = TestState.nonTerminalState("C", Collections.singletonList(new TestAction(c1)));
        B = TestState.nonTerminalState("B", Collections.singletonList(new TestAction(C)));
        A = TestState.nonTerminalState("A", Collections.singletonList(new TestAction(B)));

        DecisionTree<TestState, TestAction> tree = new DecisionTree<>(
                A, new TranspositionAdapter(), fn = new TestHeuristicEvaluationFunction(), node -> node.getState() == c1);

        tree.perform();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fn.count(s1)).isEqualTo(0);
        softly.assertThat(fn.count(c1)).isEqualTo(1);
        softly.assertAll();
    }

    class TranspositionAdapter implements Transpositions<TestState> {

        @Override
        public OptionalDouble get(TestState state) {
            return OptionalDouble.empty();
        }

        @Override
        public void put(TestState state, double utility) {
        }
    }

    class TestTranspositions extends TranspositionAdapter {

        final Map<TestState, Double> map;

        TestTranspositions() {
            map = new HashMap<>();
        }

        @Override
        public OptionalDouble get(TestState equivalence) {
            if (map.containsKey(equivalence)) {
                return OptionalDouble.of(map.get(equivalence));
            }

            return OptionalDouble.empty();
        }

        @Override
        public void put(TestState equivalences, double utility) {
            map.put(equivalences, utility);
        }
    }

    class CutoffTestAdapter implements CutoffTest<TestState> {

        @Override
        public boolean apply(Node<TestState, ?> node) {
            return false;
        }
    }

    class TestHeuristicEvaluationFunction implements HeuristicEvaluationFunction<TestState> {

        final List<TestState> evaluated;

        TestHeuristicEvaluationFunction() {
            evaluated = new LinkedList<>();
        }

        @Override
        public double apply(TestState state) {
            evaluated.add(state);
            return state.heuristicValue;
        }

        public long count(TestState state) {
            return evaluated.stream().filter(Predicate.isEqual(state)).count();
        }
    }
}
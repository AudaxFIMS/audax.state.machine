package com.audax.state.machine.graphviz;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.ObjectState;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.state.StateMachineState;
import org.springframework.statemachine.transition.Transition;

/*
** RIGHT GRAPH EXAMPLE **

digraph StateMachine {
node [style=filled];
	CANCELED;
	ORDER_COMPLETED;
	subgraph cluster_DELIVERING {
		label="DELIVERING";
		style=dashed;
		DELIVERING_DONE;
		DELIVERING_STAGE;
		DELIVERING_WAITING_LABEL;
		DELIVERING_WAITING_LABEL -> DELIVERING_STAGE [label="DELIVERY_LABEL_CREATED"];
		DELIVERING_STAGE -> DELIVERING_DONE [label="DELIVERY_DONE"];
	}
	PROCESSING;
	subgraph cluster_SHIPPING {
		label="SHIPPING";
		style=dashed;
		SHIPPING_STAGE;
		SHIPPING_WAITING_LABEL [fillcolor="lightgreen"];
		SHIPPING_DONE;
		SHIPPING_WAITING_LABEL -> SHIPPING_STAGE [label="SHIPPING_LABEL_CREATED"];
		SHIPPING_STAGE -> SHIPPING_DONE [label="SHIPPING_DONE"];
	}
	NEW;
	NEW -> CANCELED [label="CANCEL_ORDER"];
	NEW -> PROCESSING [label="PROCESS_ORDER"];
	PROCESSING -> SHIPPING_WAITING_LABEL [label="SHIPPING"];
	SHIPPING_DONE -> DELIVERING_WAITING_LABEL [label="NEXT_STEP"];
	DELIVERING_DONE -> ORDER_COMPLETED [label="NEXT_STEP"];
}

*/

public class GraphvizExporter<S, E> {
	private void addSubGraph(
			StateMachine<S, E> stateMachine,
			StringBuilder dot,
			State<S, E> parentState,
			String prefix,
			S activeState
	) {
		if (parentState != null) {
			prefix += "\t";
			dot
					.append(prefix).append("label=\"").append(parentState.getId()).append("\";\n")
					.append("\t\tstyle=dashed;\n");
		}
		// Step 1: Find all parent (composite) states
		for (State<S, E> state : stateMachine.getStates()) {
			if (state instanceof ObjectState<S, E>) {
				if (state.getId() == activeState) {
					dot.append(prefix).append(state.getId()).append(" [style=filled][fillcolor=lightgreen];\n");
				} else {
					dot.append(prefix).append(state.getId()).append(";\n");
				}
			} else if (state instanceof StateMachineState<S, E>) {
				dot.append(prefix).append("subgraph cluster_").append(state.getId()).append(" {\n");
				StateMachineState<S, E> subStateMachineState = ((StateMachineState<S, E>) state);
				addSubGraph(subStateMachineState.getSubmachine(), dot, state, prefix, activeState);
				dot.append(prefix).append("}\n");
			}
		}
		// Step 2: Add transitions between states
		for (Transition<S, E> transition : stateMachine.getTransitions()) {
			State<S, E> source = transition.getSource();
			State<S, E> target = transition.getTarget();
			
			if (source instanceof StateMachineState<S, E> || target instanceof StateMachineState<S, E>) {
				continue;
			}
			
			E event = transition.getTrigger().getEvent();
			
			dot.append(prefix).append(source.getId())
					.append(" -> ").append(target.getId())
					.append(" [label=\"").append(event).append("\"];\n");
		}
		
	}
	
	public String export(StateMachine<S, E> stateMachine, S activeState) {
		StringBuilder dot = new StringBuilder();
		
		dot.append("digraph StateMachine {\n");
		
		addSubGraph(stateMachine, dot, null, "\t", activeState);
		dot.append("}\n");
		
		return dot.toString();
	}
}

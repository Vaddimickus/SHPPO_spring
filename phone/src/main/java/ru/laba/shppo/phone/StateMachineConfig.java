package ru.laba.shppo.phone;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

import static ru.laba.shppo.phone.PhoneEvent.*;
import static ru.laba.shppo.phone.PhoneState.*;

@Configuration
@EnableStateMachine
public class StateMachineConfig {
	
	@Bean
	public StateMachine<PhoneState, PhoneEvent> stateMachine() throws Exception {
		StateMachineBuilder.Builder<PhoneState, PhoneEvent> builder = StateMachineBuilder.builder();
		
		builder
				.configureStates()
				.withStates()
					.initial(WAIT)
					.states(EnumSet.allOf(PhoneState.class));
		
		builder
				.configureTransitions()
				.withExternal()
					.event(INCOMING)
					.source(WAIT)
					.target(CALL)
				.and().withExternal()
					.event(BLOCK)
					.source(TALK)
					.target(BLOCKED)
				.and().withExternal()
					.event(TO_CALL)
					.source(WAIT)
					.target(TALK)
				.and().withExternal()
					.event(END_A_CONVERSATION)
					.source(CALL)
					.target(WAIT)
				.and().withExternal()
					.event(PICK_UP)
					.source(CALL)
					.target(TALK)
				.and().withExternal()
					.event(REPLENISH_BALANCE)
					.source(BLOCKED)
					.target(WAIT)
				.and().withExternal()
					.event(INCOMING)
					.source(BLOCKED)
					.target(CALL)
				.and().withExternal()
					.event(END_A_CONVERSATION)
					.source(TALK)
					.target(WAIT)
		;
		
		return builder.build();
	}
}

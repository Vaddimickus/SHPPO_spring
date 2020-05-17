package ru.laba.shppo.phone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class PhoneStarter implements CommandLineRunner {
	
	@Autowired
	private Phone phone;
	
	@Override
	public void run(String... args) throws Exception {
		
		phone.setNumber(880055535);
		phone.setBalance(100);
		phone.setProbability(0.1);
		
		var stateMachine = phone.getState();
		stateMachine.start();
		
		while (!stateMachine.isComplete()) {
			Thread.sleep(1000);
		}
	}
}

package ru.laba.shppo.phone;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.annotation.OnTransitionEnd;
import org.springframework.statemachine.annotation.WithStateMachine;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.laba.shppo.phone.PhoneEvent.*;
import static ru.laba.shppo.phone.PhoneState.BLOCKED;
import static ru.laba.shppo.phone.PhoneState.TALK;

@WithStateMachine
public class Phone {
	
	private int number;
	private double probability;
	
	private final AtomicInteger balance;
	private final StateMachine<PhoneState, PhoneEvent> state;
	
	public Phone(StateMachine<PhoneState, PhoneEvent> state) {
		this.state = state;
		balance = new AtomicInteger();
	}
	
	@OnTransitionEnd(target = "WAIT")
	public void doWait() {
		System.out.println("Ожидание. Выберите действие: принять_вызов, позвонить, пополнить_баланс, выход:");
		
		Scanner scanner = new Scanner(System.in);
		boolean scanning = true;
		while (scanning) {
			String answer = scanner.next();
			switch (answer) {
				case "принять_вызов" -> {
					state.sendEvent(INCOMING);
					scanning = false;
				}
				case "позвонить" -> {
					state.sendEvent(TO_CALL);
					scanning = false;
				}
				case "пополнить_баланс" -> {
					if (state.getState().getId() == BLOCKED) {
						state.sendEvent(REPLENISH_BALANCE);
						scanning = false;
					} else {
						System.out.println("Пополнение баланся доступно только при забокированном телефоне");
					}
				}
				case "выход" -> {
					System.out.println("Выход");
					scanning = false;
					state.stop();
				}
				default -> System.out.println("Неверная команда " + answer + ", попробуйте ещё раз");
			}
		}
	}
	
	@OnTransitionEnd(source = "WAIT", target = "CALL")
	public void incomingWaitToCall() {
		System.out.println("Входящий звонок (Incoming: WAIT -> CALL)");
		
		System.out.print("Ответить/отклонить? ");
		Scanner scanner = new Scanner(System.in);
		
		boolean answering = true;
		while (answering) {
			String answer = scanner.next();
			switch (answer.toLowerCase()) {
				case "ответить" -> {
					state.sendEvent(PICK_UP);
					answering = false;
				}
				case "отклонить" -> {
					state.sendEvent(END_A_CONVERSATION);
					answering = false;
				}
				default -> System.out.println("Ввод неверный: " + answer);
			}
		}
	}
	
	@OnTransitionEnd(source = "WAIT", target = "TALK")
	public void startTalk() throws InterruptedException {
		System.out.println("Начинаем говорить (Start talk: WAIT -> TALK)");
		
		new Thread(() -> {
			while (state.getState().getId() == TALK) {
				if (balance.get() > 0) {
					balance.addAndGet(-10);
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					System.out.println("Баланс отрицательный");
					state.sendEvent(BLOCK);
					return;
				}
			}
		}).start();
		
		Scanner scanner = new Scanner(System.in);
		while (state.getState().getId() == TALK) {
			System.out.print("Говорите: ");
			String input = scanner.next();
			
			if (input.equalsIgnoreCase("завершить")) {
				System.out.println("Завершаем разговор");
				state.sendEvent(END_A_CONVERSATION);
				return;
			}
			
			System.out.println("Эхо: " + input + ". Баланс = " + balance.get());
		}
	}
	
	@OnTransitionEnd(source = "CALL", target = "WAIT")
	public void endAConversationCallToWait() {
		System.out.println("Кладём трубку (End a conversation: CALL -> WAIT)");
	}
	
	@OnTransitionEnd(source = "CALL", target = "TALK")
	public void pickUp() throws InterruptedException {
		System.out.println("Поднимаем тубку (Pick up: CALL -> TALK)");
		
		for (int i = 0; i < 5; i++) {
			System.out.println("Слушаем (CoolStoryBob)");
			Thread.sleep(1000);
		}
		
		state.sendEvent(END_A_CONVERSATION);
	}
	
	@OnTransitionEnd(source = "BLOCKED", target = "WAIT")
	public void replenishBalance() {
		System.out.println("Пополняем баланс (Replenish balance: BLOCK -> WAIT)");
		
		System.out.println("Введите сумму для пополения баланса");
		Scanner scanner = new Scanner(System.in);
		
		while (true) {
			try {
				balance.addAndGet(scanner.nextInt());
				break;
			} catch (RuntimeException e) {
				System.out.println("Неправильно введён баланс, повторите ввод");
			}
		}
	}
	
	@OnTransitionEnd(source = "BLOCKED", target = "CALL")
	public void incomingBlockedToCall() {
		System.out.println("Принимаем звонок, пока баланс на нуле (Incoming: BLOCKED -> CALL)");
	}
	
	@OnTransitionEnd(source = "TALK", target = "WAIT")
	public void endAConversationTalkToWait() {
		System.out.println("Заканчиваем звонок (End a conversation: TALK -> WAIT)");
	}
	
	@OnTransitionEnd(source = "TALK", target = "BLOCKED")
	public void block() {
		System.out.println("Заканчиваем звонок - Баланс отрицательный (Block: TALK -> BLOCKED)");
	}
	
	public int getNumber() {
		return number;
	}
	
	public void setNumber(int number) {
		this.number = number;
	}
	
	public int getBalance() {
		return balance.get();
	}
	
	public void setBalance(int balance) {
		this.balance.set(balance);
	}
	
	public double getProbability() {
		return probability;
	}
	
	public void setProbability(double probability) {
		this.probability = probability;
	}
	
	public StateMachine<PhoneState, PhoneEvent> getState() {
		return state;
	}
}

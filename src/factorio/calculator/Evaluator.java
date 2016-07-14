package factorio.calculator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Evaluator {

	private Evaluator() {}

	private static final Pattern TOKEN = Pattern.compile("[\\+\\-\\*\\/\\(\\)]|(?:\\-?\\d+)");

	public static void main(String args[]) {
		System.out.println(evaluate("(3 + 4) * 2 / ( 1 - 5 )"));
	}

	public static List<String> toPostfix(String infix) {
		ArrayDeque<String> stack = new ArrayDeque<>(), oper = new ArrayDeque<>();

		Matcher m = TOKEN.matcher(infix.replaceAll("\\s", "").replaceAll("([\\+\\-\\*\\/])+(\\d)", "$1$2"));

		boolean lastOper = true;
		while (m.find()) {
			String token = m.group();

			try {
				Double.parseDouble(token);
				stack.push(token);
				lastOper = false;
			} catch (NumberFormatException e) {
				if (lastOper && token.equals("-")) {
					m.find();
					String num = m.group();
					stack.push("-" + num);
					lastOper = false;
				} else if (token.equals(")")) {
					lastOper = true;
					while (!oper.peek().equals("(")) {
						stack.push(oper.pop());
					}
					oper.pop();
				} else {
					lastOper = true;
					while (!(oper.isEmpty() || oper.peek().equals("(") || precedence(oper.peek().charAt(0)) < precedence(token.charAt(0)))) {
						stack.push(oper.pop());
					}
					oper.push(token);
				}
			}
		}

		while (!oper.isEmpty()) {
			stack.push(oper.pop());
		}

		return new ArrayList<>(stack);
	}

	private static int precedence(char oper) {
		return oper == '(' ? 2 : (oper == '/' || oper == '*' ? 1 : 0);
	}
	
	public static double evaluate(List<String> postfix) {
		List<String> pfx = new ArrayList<>(postfix);
		Collections.reverse(pfx);
		
		ArrayDeque<Double> stack = new ArrayDeque<>();

		for (String token : pfx) {
			try {
				double d = Double.parseDouble(token);
				stack.push(d);
			} catch (NumberFormatException e) {
				switch (token) {
					case "+":
						stack.push(stack.pop() + stack.pop());
						break;
					case "-":
						stack.push(-stack.pop() + stack.pop());
						break;
					case "*":
						stack.push(stack.pop() * stack.pop());
						break;
					case "/":
						stack.push(1 / (stack.pop() / stack.pop()));
						break;
				}
			}
		}
		
		return stack.peek();
	}
	
	public static double evaluate(String infix) {
		return evaluate(toPostfix(infix));
	}

}

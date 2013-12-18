package sample.input;

import java.util.Arrays;

public class LittleNested {
	private boolean cond;

	void  nestedCases() {
		if (cond) {     // 1
			if (cond) {   // 0.5
				someExpression();  // 0.25
			}
			else {  // 0.5
				if (cond) { //0.25
					someExpression(); // 0.125
				} else if (cond) {  // 0.25
					someExpression(); // 0.125
				}
			}
		}
		
		while (cond) {  // 1
			if (cond) {   // 0.5
				SomeLabel:try { // 0.25
					
				}
				finally {   // 0.25
					for(int i = 0; i < 0; ++i) { // 0.25
						do              // 0.125
							if (cond)     // 0.0625
								break;      // 0.03125
							else if (cond)  // 0.0625
								break SomeLabel; // 0.03125
							else              // 0.0625
								synchronized (someExpression()) { // 0.03125
									switch (new String("").length()) { // 0.03125
									case 3: // 0.03125
										class LocalClass { // 0.015625
											void innerMethod() { // 0. 015625
												try { // 0. 015625
													
												}
												catch (Throwable t) { // 0. 015625
													;
												}
											}
										}
									}
								}
						while(cond); // 0.125
					}
				}
			}
		}
	}

	private Object someExpression() {
		return null;		
	}
}

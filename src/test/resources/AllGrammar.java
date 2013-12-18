package sample.input;

public class AllGrammar {
	private boolean cond;

	public void  allStatements() throws Exception {
		int  localVariableDeclarationStatement = 0; // 1
		class ClassOrInterfaceDeclarartionStatement // 1
		{
			int  someField; // 0 1
		}
				
		{
			{
				int  a = 0; // 1
				a = a + 0; // 1
			}
		}
		
		assert cond; // 1
		assert cond : 10; // 1
		
		if (cond) // 1
			someStatement(); // 0.5
	
		if (cond) // 1
			someStatement();// 0.5
		else // 1
			someStatement();// 0.5
		
		for (int  i = 0; i < 10; ++i)// 1
			someStatement();// 0.5
		
		for (int  i : new int[10])// 1
			someStatement();// 0.5
		
		while (cond) {// 1
			someStatement();// 0.5
		}
		
		someLabel: while (cond) {// 1
			while (cond) {// 0.5
				break someLabel; //0.25
			}
		}
		
		do {// 1
			someStatement();// 0.5
			continue;// 0.5
		}
		while (cond);// 1
		
		try {// 1
			someStatement();// 1
		}
		catch (Exception e) {// 1
			someStatement();// 1
		}
		
		try {// 1
			someStatement();// 1
		}
		finally {// 1
			someStatement();// 1
		}
		
		try {// 1
			someStatement();// 1
		}
		catch (Exception e) {// 1
			throw e;// 1
		}
		finally {// 1
			someStatement();// 1
		}
		
//		try (AutoCloseable  a = new AutoCloseable() {
//
//			@Override
//			public void close() throws Exception {				
//			}
//			
//		}) {
//			someStatement();
//			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} finally {
//		}

        
		synchronized (someExpression()) {// 1
			someStatement();// 1
		}
		
		{
			;
		}
		
		someExpression();// 1
	}

	private Object someExpression() {
		// TODO Auto-generated method stub
		return null;
	}

	private void someStatement() {
		// TODO Auto-generated method stub
		
	}
}

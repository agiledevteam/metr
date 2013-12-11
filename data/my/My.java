package my;

interface Abs {
	void callback();
}

class Your<T extends Abs> {
	int state;
	T field;
	{
	  state = 0;
	  field = null;
	}
	// 1
	boolean ready() {
	  int a = 0;
	  if (a < 0) {
	    a++;
	  } else if (a < 0) {
	    a++;
	    a++;
	  }
	  if (false
        && true
        || true
        && !true
        && false
        && true
        || true
        && !true
        && false
        && true
        || true
        && !true
        && false
        && true
        || true
        && !true) a++; else a--;
		return false;
	}
	// 3
	int state() {
		if (state < 0) {
			throw new IllegalStateException();
		}
		return state;
	}
	// 3
	void background() {
		while (state > 0) {
			System.out.println("background work");
			state -- ;
		}
	}
	// 5
	void process(T abs) {
		if (ready()) {
			
			if (state() > 0) {
				for (int i = 0; i<100; i++) {
					abs.callback();
					state++;
				}
			}
		}
	}
}


public class My implements Abs {
	private Your<My> your;
	// 1
	public My(Your<My> your) {
		this.your = your;
	}
	
	// 8
	void process() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				your.background();
			}
		}).start();
		if (!your.ready()) {
			return;
		}
		your.process(this);
	}
	
	// loc == 13
	@Override
	public void callback() {
	  // 1
	  int state = your.state(); 
	  // 7
		switch (state) {
		case 0:
			break;
		case 1:
			break;
		default:
			break;
		}
		
		// 4
		Abs a = new Abs() {
      @Override
      public void callback() {
        System.out.println();
      }
    };
		//1
		new Your<Abs>().process(a);
	}
	
	int plainLoc(String src) {
	  int line=0;
	  for (String s : src.split("\n")) {
	    if (blankLine(s))
	      line++;
	  }
	  return line;
	}
	// all chars are not letter
	boolean blankLine(String line) {
	  for (int i=0; i<line.length(); i++) {
	    if (!blankChar(line.charAt(i))) {
	      return false;
	    }
	  }
	  return true;
	}
	
	boolean blankChar(char c) {
	  return !Character.isLetterOrDigit(c);
	}
}
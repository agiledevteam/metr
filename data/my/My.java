package my;

interface Abs {
	void callback();
}

class Your<T extends Abs> {
	int state = 0;
	T field;
	
	boolean ready() { 
		return false;
	}
	
	int state() {
		if (state < 0) {
			throw new IllegalStateException();
		}
		return state;
	}
	void background() {
		while (state > 0) {
			System.out.println("background work");
			state -- ;
		}
	}
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
	public My(Your<My> your) {
		this.your = your;
	}
	
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
	
	@Override
	public void callback() {
		switch (your.state()) {
		case 0:
			break;
		case 1:
			break;
		default:
			break;
		}
		
		new Your<Abs>().process(new Abs() {
			@Override
			public void callback() {
				System.out.println();
			}
		});
	}
}
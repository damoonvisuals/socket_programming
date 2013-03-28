public class IntervalTimer {

	public IntervalTimer() {
		reset();
	}

	public void start() {
		running = true;
		tStart = System.currentTimeMillis();
		tFinish = tStart;
	}

	public long stop() {
		tFinish = System.currentTimeMillis();
		if (running) {
			running = false;

			long diff = (tFinish - tStart);
			tAccum += diff;
			return diff;
		}
		return 0;
	}

	public long elapsed() {
		if (running)
			return System.currentTimeMillis() - tStart;

		return tAccum;
	}

	public void reset() {
		running = false;
		tStart = tFinish = 0;
		tAccum = 0;
	}

	private boolean running;
	private long tStart;
	private long tFinish;
	private long tAccum; // total time
}

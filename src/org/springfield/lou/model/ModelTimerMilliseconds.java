package org.springfield.lou.model;import org.springfield.fs.FsNode;

public class ModelTimerMilliseconds extends Thread {

		private boolean running = false;
		private ModelEventManager em;
		private long mm10counter = 0; // times 5 so 
		private long mm20counter = 0;
		private long mm50counter = 0;
		private long mm100counter = 0;
		
		public ModelTimerMilliseconds(ModelEventManager em) {
			super("model mm thread");
			running = true;
			this.em = em;
			start();
			
		}		
		
		public void run() {
			while (running) {
				try {	
					sleep(50);
					//System.out.print("-");
					mm10counter++;
					// the one second is allways
					FsNode tnode = new FsNode("timer","50ms");
					tnode.setProperty("value",""+mm10counter);
					tnode.setProperty("seconds",""+mm10counter);
					em.notify("/shared[timers]/50ms",tnode);
					//System.out.println("model timer "+this.hashCode());
					
					if (mm10counter%2==0) {
						mm20counter++;
						tnode = new FsNode("timer","100ms");
						tnode.setProperty("value",""+mm20counter);
						tnode.setProperty("seconds",""+mm20counter);
						em.notify("/shared[timers]/100ms",tnode);
					}
					
					if (mm10counter%5==0) {
						mm50counter++;
						tnode = new FsNode("timer","250ms");
						tnode.setProperty("value",""+mm50counter);
						tnode.setProperty("seconds",""+mm50counter);
						em.notify("/shared[timers]/250ms",tnode);
					}
					
					if (mm100counter%100==0) {
						mm100counter++;
						tnode = new FsNode("timer","500ms");
						tnode.setProperty("value",""+mm100counter);
						tnode.setProperty("seconds",""+mm100counter);
						em.notify("/shared[timers]/500ms",tnode);
					}
					
					
				} catch(InterruptedException i) {
					if (!running) break;
				} catch(Exception e) {
						System.out.println("ERROR ModelTimer Thread");
						e.printStackTrace();
				}
			}
		}
		
	    /**
	     * Shutdown
	     */
		public void destroy() {
			System.out.println("DESTOOOYYY MODEL CALLED");
			running = false;
			this.interrupt(); // signal we should stop;
		}

}

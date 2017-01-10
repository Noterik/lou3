package org.springfield.lou.model;import org.springfield.fs.FsNode;

public class ModelTimer extends Thread {

		private boolean running = false;
		private ModelEventManager em;
		private long secondscounter = 0;
		private long twosecondscounter = 0;
		private long fivesecondscounter = 0;
		private long tensecondscounter = 0;
		
		public ModelTimer(ModelEventManager em) {
			super("model thread");
			running = true;
			this.em = em;
			start();
		}		
		
		public void run() {
			while (running) {
				try {	
					sleep(1000);
					secondscounter++;
					// the one second is allways
					FsNode tnode = new FsNode("timer","1second");
					tnode.setProperty("value",""+secondscounter);
					tnode.setProperty("seconds",""+secondscounter);
					em.notify("/shared[timers]/1second",tnode);
					//System.out.println("model timer "+this.hashCode());
					
					if (secondscounter%2==0) {
						twosecondscounter++;
						tnode = new FsNode("timer","2second");
						tnode.setProperty("value",""+twosecondscounter);
						tnode.setProperty("seconds",""+secondscounter);
						em.notify("/shared[timers]/2second",tnode);
					}
					
					if (secondscounter%5==0) {
						fivesecondscounter++;
						tnode = new FsNode("timer","5second");
						tnode.setProperty("value",""+fivesecondscounter);
						tnode.setProperty("seconds",""+secondscounter);
						em.notify("/shared[timers]/5second",tnode);
					}
					
					if (secondscounter%10==0) {
						tensecondscounter++;
						tnode = new FsNode("timer","10second");
						tnode.setProperty("value",""+tensecondscounter);
						tnode.setProperty("seconds",""+secondscounter);
						em.notify("/shared[timers]/10second",tnode);
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

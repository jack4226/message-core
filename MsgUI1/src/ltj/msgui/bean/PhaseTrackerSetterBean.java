package ltj.msgui.bean;

import javax.faces.FactoryFinder;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.model.SelectItem;

public class PhaseTrackerSetterBean {
	private SelectItem[] phases = { new SelectItem("RESTORE_VIEW"), new SelectItem("APPLY_REQUEST_VALUES"),
			new SelectItem("PROCESS_VALIDATIONS"), new SelectItem("UPDATE_MODEL_VALUES"),
			new SelectItem("INVOKE_APPLICATION"), new SelectItem("RENDER_RESPONSE"), new SelectItem("ANY_PHASE"), };

	public SelectItem[] getPhases() {
		return phases;
	}

	public void phaseChange(ValueChangeEvent e) {
		LifecycleFactory factory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
		Lifecycle lifecycle = factory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);

		PhaseListener[] listeners = lifecycle.getPhaseListeners();
		for (int i = 0; i < listeners.length; i++) {
			PhaseListener listener = listeners[i];
			if (listener instanceof ltj.msgui.listener.PhaseTracker) {
				((ltj.msgui.listener.PhaseTracker) listener).setPhase((String) e.getNewValue());
			}
		}
	}

	public void afterPhase(PhaseEvent event) {
		System.out.println("AFTER PHASE " + showEvent(event));
	}

	public void beforePhase(PhaseEvent event) {
		System.out.println("BEFORE PHASE " + showEvent(event));
	}

	private String showEvent(PhaseEvent event) {
		return "Phase Event: " + event.getPhaseId();
	}
}

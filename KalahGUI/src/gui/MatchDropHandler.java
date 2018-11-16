package gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.TransferHandler;

import gui.ActivityListener.Level;

@SuppressWarnings("serial")
public class MatchDropHandler extends TransferHandler{

	public boolean canImport(TransferHandler.TransferSupport info) {
		return true;
		//return !info.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
	}
	
	@SuppressWarnings("unchecked")
	public boolean importData(TransferHandler.TransferSupport info) {
		if(!info.isDrop()) return false;
		if(!info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) return false;
		List<File> filesDropped;
		try {
			filesDropped = (List<File>) info.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
		} catch (UnsupportedFlavorException | IOException | ClassCastException e) {
			Coordinator.log("Could not import dropped file(s): " + e.getMessage(), Level.ERROR);
			return false;
		}
		if(filesDropped.size() != 1) {
			Coordinator.log("Dropping of multiple matches is not supported currently. I'll pick the last one dropped.", Level.WARN);
		}
		for(File f : filesDropped) {
			Coordinator.setActiveMatch(f);
		}
		return true;
	}
}

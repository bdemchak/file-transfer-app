package org.cytoscape.file_transfer.internal;

import java.io.File;
import java.nio.file.*;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class ToSandboxTask extends CyRESTAbstractTask {
	
	@ProvidesTitle
	public String getTitle() { return "Transfer file to sandbox"; }

	@Tunable (description="sandboxName", longDescription="Name of sandbox to contain file", exampleStringValue="mySandbox")
	public String sandboxName = "";
	
	@Tunable (description="fileName", longDescription="Sandbox-relative name of file to save.", exampleStringValue="myFile.png")
	public String fileName = "";
	
	@Tunable (description="fileBase64", longDescription="The file content as Base64", exampleStringValue="iVBORw0KGgoAAAANSUhEUgAABY=")
	public String fileBase64 = "";

	@Tunable (description="fileByteCount", longDescription="The count of bytes in the raw file", exampleStringValue="10")
	public long fileByteCount = 0;

	@Tunable (description="overwrite", longDescription="True to overwrite a file if it already exists", exampleStringValue="true")
	public boolean overwrite = false;

	private File sandboxParentDirFile;
	
	
	public ToSandboxTask(File sandboxParentDirFile){
		super();
		this.sandboxParentDirFile = sandboxParentDirFile;
	}
	
	@Override
	public void run(TaskMonitor taskMon) throws Exception {
		File fileFile = SandboxUtils.getAbsFileFile(sandboxParentDirFile, sandboxName, fileName, false);
		if (fileFile.exists()) {
			if (overwrite) {
				try {
					FileUtils.forceDelete(fileFile); // Kill file or directory
				} catch(Throwable e) {}
			} else {
				throw new Exception("'" + fileName + "' already exists.");
			}
		}

		if (fileBase64 == null || fileBase64.length() == 0) {
			throw new Exception("File content cannot be empty.");
		}
		
		byte[] fileRaw = Base64.decodeBase64(fileBase64);
		if (fileRaw.length != fileByteCount) {
			throw new Exception("File '" + fileName + "' contains " + fileRaw.length + " bytes but should contain " + fileByteCount + " bytes");
		}
		
		taskMon.setStatusMessage("Writing file " + fileName);
		String filePath = fileFile.getCanonicalPath();
		Files.write(Paths.get(filePath), fileRaw);
		
		result = new ToSandboxResult(filePath);
	}
	
	public static String getExample() {
		return getJson(new ToSandboxResult("/User/CytoscapeConfiguration/FileTransfer/MySandbox/MyFile.png"));
	}
}
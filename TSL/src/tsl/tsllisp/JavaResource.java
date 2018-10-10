package tsl.tsllisp;

public class JavaResource extends JavaObject {
	private int type = 0;
	
	static int FileType = 1;
	static int DirectoryType = 2;
	
	public JavaResource(int type, Object object) {
		super(object);
		this.type = type;
	}
	
	public boolean isFile() {
		return this.type == FileType;
	}
	
	public boolean isDirectory() {
		return this.type == DirectoryType;
	}
}

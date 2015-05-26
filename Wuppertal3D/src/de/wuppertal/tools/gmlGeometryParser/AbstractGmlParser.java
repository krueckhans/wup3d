package de.wuppertal.tools.gmlGeometryParser;

import java.io.File;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;

import com.jme3.math.Triangle;
import com.jme3.math.Vector3f;

import de.wuppertal.Coord;
import de.wuppertal.CoordVector;

public abstract class AbstractGmlParser {
	private Vector<File> files2Parse;
	private volatile int iRunningThreads;
	private volatile int iFiles2Parse;
	private volatile int iFilesParsed;

	public AbstractGmlParser(Vector<File> files2Parse){
		this.files2Parse = files2Parse;
		iFiles2Parse = 0;
		iFilesParsed=0;
	}

	public AbstractGmlParser(File fileRoot){
		files2Parse = new Vector<File>();
		iFiles2Parse = 0;
		iFilesParsed=0;
		for(File f:fileRoot.listFiles()){
			if(isGML(f)){
				files2Parse.add(f);
			}
		}
	}

	private boolean isGML(File f){
		String ending = f.getName();
		if(ending.endsWith(".gml") || ending.endsWith(".GML")){
			return true;
		}
		return false;
	}


	public void startParsing(){
		System.out.println("start Parsing");
		for(File f:files2Parse){
			parseFile(f);
		}
	}

	public void startParsingMultiThreaded(int iThreads){
		iFiles2Parse = files2Parse.size();
		Thread threads[] = new Thread[iThreads];
		final Vector<File>[] files4Thread = getFiles4Thread(iThreads);
		for(int i=0;i<iThreads;i++){
			final int iThreadId = i;
			Thread t = new Thread(){
				public void run(){
					try{
						setName("ParsingThread "+iThreadId);
						for(File f:files4Thread[iThreadId]){
							parseFile(f);
							iFilesParsed++;
							System.out.println("Parsed File "+iFilesParsed+" of "+iFiles2Parse);
						}
					}
					catch(Exception e){
						e.printStackTrace();
					}
					finally{
						iRunningThreads--;
					}
				}
			};
			t.start();
			iRunningThreads++;
			threads[i] =t;
		}
		while(iRunningThreads>0){
			try{
				Thread.sleep(100);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		allFilesFinished();
	}

	public void allFilesFinished() {
		
	}

	private Vector<File>[] getFiles4Thread(int iThreads){
		Vector<File>[] files4Thread = new Vector[iThreads];
		for(int iThread=0;iThread<iThreads;iThread++){
			files4Thread[iThread] = new Vector<File>();
		}
		int i=0;
		for(File f:files2Parse){
			files4Thread[i%iThreads].add(f);
			i++;
		}
		return files4Thread;
	}

	private boolean parseFile(File f){
		try{
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(f);
			Iterator<Element> processDescendants = doc.getDescendants(new ElementFilter()); 
			while(processDescendants.hasNext()) {
				Element e =  processDescendants.next();
				String currentName = e.getName();
				parseGMLClass(currentName,f,e,processDescendants);
			}
			finishedParsing(f);
			return true;

		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	protected abstract void parseGMLClass(String currentName,File f,Element e,Iterator<Element> processDescendants) ;

	public void parsePosList(File f,Element e, GMLTYPE gmlType,Iterator<Element> processDescendants){
		StringTokenizer st = new StringTokenizer(e.getValue()," ");
		double x,y,z;
		CoordVector vec = new CoordVector();
		while(st.hasMoreTokens()){
			x = Double.parseDouble(st.nextToken());
			y = Double.parseDouble(st.nextToken());
			z = Double.parseDouble(st.nextToken());
			vec.add(new Coord(x, y, z) );
		}
		handleCoordVector(vec,f,e,gmlType,processDescendants);
	}

	protected abstract void handleCoordVector(CoordVector vec,File f, Element e, GMLTYPE gmlType,Iterator<Element> processDescendants);

	protected abstract void finishedParsing(File f);
	

	public Vector3f getNormal(Vector3f p00, Vector3f p10, Vector3f p01){
		Triangle t = new Triangle(p00, p10, p01);
		t.calculateNormal();
		Vector3f vecNormal = t.getNormal();
		return vecNormal;
	}

	public Vector3f getNormal(CoordVector vecTriangle){
		return getNormal(vecTriangle.getCoord(0),vecTriangle.getCoord(1),vecTriangle.getCoord(2));
	}

	public Vector3f getNormal(Coord p0,Coord p1,Coord p2){
		return getNormal(p0.getVec3f(),p1.getVec3f(), p2.getVec3f());
	}
}

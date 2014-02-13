package org.soul.newWord.crf;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.soul.library.PersonDetails;

public class PersonPersist {

	public static void main(String[] args) {
		String filename = "/home/liubo/person.txt";
		PersonDetails person1 = new PersonDetails("hemanth", 10, "Male");
		PersonDetails person2 = new PersonDetails("bob", 12, "Male");
		PersonDetails person3 = new PersonDetails("Richa", 10, "Female");
		List list = new ArrayList();
		list.add(person1);
		list.add(person2);
		list.add(person3);
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(filename);
			out = new ObjectOutputStream(fos);
			out.writeObject(list);
			out.close();
			System.out.println("Object Persisted");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
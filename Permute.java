import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Permute {
	//BEST powerset code (perfect) http://stackoverflow.com/questions/4640034/calculating-all-of-the-subsets-of-a-set-of-numbers
	static List<List<String>> permutations(final List<Integer> values, int ind) {
		if (ind == values.size()) 
			return new ArrayList<>();
			int thisVal = values.get(ind);
			List<List<String>> subset = permutations(values, ind + 1);
			List<List<String>> returnList = new ArrayList<>();
			returnList.add(Arrays.asList(String.valueOf(thisVal)));
			returnList.addAll(subset);
			for (final List<String> subsetValues : subset) 
				for (final String subsetValue : subsetValues) 
					returnList.add(Arrays.asList(thisVal + "," + subsetValue));
			return returnList;
	}

	//essentially http://stackoverflow.com/questions/16242733/sum-all-the-elements-java-arraylist but very simple 
	public static int sum(List<String> list){
		int tot = 0; 
		for(int ii = 0; ii < list.size(); ii ++){
			String str = list.get(ii); 
			String[] split = str.split(","); 
			for (int jj = 0; jj < split.length; jj++) {
				tot += Integer.valueOf(split[jj]); 
			}
		}
		return tot;
	}

	//http://stackoverflow.com/questions/16789504/printing-out-2d-array-elements-in-java 
	public static void print(String[][] plants){
		for (int ii = 0; ii < plants.length; ii++) 
			for (int jj = 0; jj < plants[0].length; jj++) 
				if(plants[ii][jj] == "_") 
					plants[ii][jj] = " "; //replace -'s with spaces to print 

		for(int ii = 0; ii < plants.length; ii++){
			for(int jj = 0; jj < plants[ii].length; jj++)
				System.out.print(plants[ii][jj] + " ");
			System.out.println();
		}
	}
}
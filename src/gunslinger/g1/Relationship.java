package gunslinger.g1;

import java.util.Random;

public class Relationship {
	Random rand=new Random(0);
	boolean[][] fixed;
	double[][] friend;
	double[][] neutral; //neutral_i_j means the probability i think j is his neutral
	int n, me, e, f, round;
	int [][] history;

	public Relationship(int nplayers, int[] friends, int[] enemies, int id) {
		n=nplayers;
		round=-1;
		fixed=new boolean[n][n];
		friend=new double[n][n];
		neutral=new double[n][n];
		history=new int[20][n];
		me=id;
		e=enemies.length;
		f=friends.length;
		
		for (int i = 0; i < n; i++) {
			for(int j=0;j<n;j++)
				if (i!=j){
					friend[i][j]=((double)f)/(n-1);
					neutral[i][j]=((double)n-1-e-f)/(n-1);
				}
		}
		
		for(int i=0;i<n;i++){
			fixed[me][i]=true;
			neutral[me][i]=1;
		}
		
		for(int i=0;i<friends.length;i++){
			int j=friends[i];
			fixed[j][me]=true;
			friend[j][me]=1;
			friend[me][j]=1;
			neutral[me][j]=0;
			neutral[j][me]=0;
			for (int k = 0; k < n; k++) 
				if(k!=me){
					friend[j][k]=(f-1)/(n-1);
				}
		}
		
		for(int i=0;i<enemies.length;i++){
			int j=enemies[i];
			fixed[me][j]=true;
			friend[me][j]=0;
			neutral[me][j]=0;
		}
		
		
	}
	double enemy_constant1=0.3;
	double enemy_constant2=0.3;
	double friend_constant=0.9;
	
	public void update(int[] prevRound,boolean[] alive) {
		if (prevRound == null || prevRound.length == 0){
            return;
        }
		round++;
		for (int i = 0; i < n; i++) {
			history[round][i]=prevRound[i];
		}
		
		//enemy inference
		for (int i = 0; i < n; i++) 
		if(prevRound[i]>=0){
			int j=prevRound[i];
			if (!fixed[i][j]){
				friend[i][j]*=enemy_constant1;
				neutral[i][j]*=enemy_constant1;
			}
			//retaliation
			if (!fixed[j][i]){
				friend[j][i]*=enemy_constant2;
				neutral[j][i]*=enemy_constant2;
			}
		}
		//friend inference
		if(round>0)
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i!=j) 
					if(history[round-1][i]==prevRound[j]){
						if (!fixed[i][j]){
							friend[i][j]=1-(pE(i,j)+pN(i,j))*friend_constant;
							neutral[i][j]*=friend_constant;
						}
						if (!fixed[j][i]){
							friend[j][i]=1-(pE(j,i)+pN(j,i))*friend_constant;
							neutral[j][i]*=friend_constant;
						}
					}
			}
		}
		if (round>5){
			System.out.println();
		}
		//dead inference
		//TODO
	}
	
	public double pN(int i, int j) {
		return neutral[i][j];
	}

	public double pF(int i, int j) {
		return friend[i][j];
	}

	public double pE(int i, int j) {
		return 1-friend[i][j]-neutral[i][j];
	}
	
	
	//return 0--neutral  1--friend  2--enemy
	public int[][] sample(){
		int[][] a=new int[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) 
			if(i!=j){
				double t=rand.nextDouble();
				if(t<pN(i, j)){ 
					a[i][j]=0;
				}
				else if (t<pN(i, j)+pF(i, j)) {
					a[i][j]=1;
				}
				else {
					a[i][j]=2;
				}
			}
			
		}
		return a;
	}

}

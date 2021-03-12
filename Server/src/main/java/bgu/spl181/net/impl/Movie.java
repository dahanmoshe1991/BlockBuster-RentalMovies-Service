package bgu.spl181.net.impl;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Movie {

	@SerializedName("id")
	private String id;
	@SerializedName("name")
	private String name;
	@SerializedName("price")
	private String price;
	@SerializedName("bannedCountries")
	private List<String> bannedCountries;
	@SerializedName("availableAmount")
	private String availableAmount;
	@SerializedName("totalAmount")
	private String totalAmount;

	//constructor
	public Movie(String moviename, String amount, String price, List<String> banned, String id) {
		
		this.name = moviename;
		this.price = price;
		this.availableAmount = amount;
		this.totalAmount = amount;
		this.bannedCountries = banned;
		this.id= id;
	}

	public String getName() {						//getter movie name
		return name;
	}

	public String getId() {							//getter id
		return id;
	}

	public String getPrice() {						//getter price
		return price;
	}
	
	public List<String> getBannedCountries() {		//getter banned countries
		return bannedCountries;
	}

	public String getAvailableAmount() {			//getter available amount
		return availableAmount;
	}

	public String getTotalAmount() {				//getter total amount
		return totalAmount;
	}

	public String getBannedCountriesString() {		//changes list to string
		
		String ans = "";
		if (!bannedCountries.isEmpty()) {
			String[] countries = new String[bannedCountries.size()];
			bannedCountries.toArray(countries);
			for(int i = 0; i < countries.length; i++)
				ans = ans + "\"" + countries[i] + "\"" +" ";
			ans = ans.substring(0, ans.length() -1);
		}
		return ans;
	}

	public void addCopies(int i) {

		int newAmount = Integer.parseInt(availableAmount) + i;
		availableAmount = String.valueOf(newAmount);
	}
	
	@Override
	public boolean equals(Object mov) {
		return (this.getName().equals(((Movie) mov).getName()));
	}

	public void changePrice(String newPrice) {
			this.price = newPrice;
	}
}

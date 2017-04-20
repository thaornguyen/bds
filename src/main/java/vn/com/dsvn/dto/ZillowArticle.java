package vn.com.dsvn.dto;

import org.json.JSONObject;

public class ZillowArticle {
	private String link;
	private String address;
	private String description;
	private long buyCost;
	private long rentCost;
	private String numBeds;
	private String numBaths;
	private String sqft;
	private String sqftLot;
	private String status;
	private String builtYear;
	private String lotAc;

	private String additionalFeatures;
	private String openHouse;

	private JSONObject facts;
	private JSONObject features;
	private JSONObject appliancesIncluded;
	private JSONObject roomTypes;
	private JSONObject construct;
	private JSONObject others;

	public String toJson() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("URL", this.link);
		jsonObject.put("Address", this.address);
		jsonObject.put("Description", this.description);
		// cost
		JSONObject objCost = new JSONObject();
		objCost.put("Buy", this.buyCost);
		objCost.put("Rent", this.rentCost);
		jsonObject.put("Cost", objCost);

		jsonObject.put("Num Beds", this.numBeds);
		jsonObject.put("Num Baths", this.numBaths);
		jsonObject.put("Square foot", this.sqft);
		jsonObject.put("Status", this.status);
		jsonObject.put("Open house", this.openHouse);
		jsonObject.put("Facts", this.facts);
		jsonObject.put("Features", this.features);
		jsonObject.put("Additional Features", this.additionalFeatures);
		jsonObject.put("Appliances Included", this.appliancesIncluded);
		jsonObject.put("Room Types", this.roomTypes);
		jsonObject.put("Construction", this.construct);
		jsonObject.put("Other", this.others);

		return jsonObject.toString();
	}

	public String toSimpleJson() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("URL", this.link);
		jsonObject.put("Address", this.address);
		jsonObject.put("Cost", this.buyCost);
		jsonObject.put("Num Beds", this.numBeds);
		jsonObject.put("Num Baths", this.numBaths);
		jsonObject.put("Square", this.sqft);
		jsonObject.put("Square Lot", this.sqftLot);
		jsonObject.put("Status", this.status);
		jsonObject.put("Built year", this.builtYear);
		jsonObject.put("LotAcre", this.lotAc);
		return jsonObject.toString();
	}

	public void setSqftLot(String sqftLot) {
		this.sqftLot = sqftLot;
	}

	public String getLink() {
		return link;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public void setBuiltYear(String builtYear) {
		this.builtYear = builtYear;
	}

	public void setLotAc(String lotAc) {
		this.lotAc = lotAc;
	}

	public void setOpenHouse(String openHouse) {
		this.openHouse = openHouse;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setBuyCost(long buyCost) {
		this.buyCost = buyCost;
	}

	public void setRentCost(long rentCost) {
		this.rentCost = rentCost;
	}

	public void setNumBeds(String numBeds) {
		this.numBeds = numBeds;
	}

	public void setNumBaths(String numBaths) {
		this.numBaths = numBaths;
	}

	public void setSqft(String sqft) {
		this.sqft = sqft;
	}

	public void setAdditionalFeatures(String additionalFeatures) {
		this.additionalFeatures = additionalFeatures;
	}

	public void setFacts(JSONObject facts) {
		this.facts = facts;
	}

	public void setFeatures(JSONObject features) {
		this.features = features;
	}

	public void setAppliancesIncluded(JSONObject appliancesIncluded) {
		this.appliancesIncluded = appliancesIncluded;
	}

	public void setRoomTypes(JSONObject roomTypes) {
		this.roomTypes = roomTypes;
	}

	public void setConstruct(JSONObject construct) {
		this.construct = construct;
	}

	public void setOthers(JSONObject others) {
		this.others = others;
	}

}

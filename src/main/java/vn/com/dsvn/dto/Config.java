package vn.com.dsvn.dto;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Config {
	private String addressTag;
	private String buyCostTag;
	private String rentCostTag;
	private String desTag;
	private String roomTag;
	private String featureTag;
	private String groupFeatureTag;

	public static Config parse(File json) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		Config config = null;
		config = mapper.readValue(json, Config.class);
		return config;
	}

	public String getGroupFeatureTag() {
		return groupFeatureTag;
	}

	public void setGroupFeatureTag(String groupFeatureTag) {
		this.groupFeatureTag = groupFeatureTag;
	}

	public String getFeatureTag() {
		return featureTag;
	}

	public void setFeatureTag(String featureTag) {
		this.featureTag = featureTag;
	}

	public String getDesTag() {
		return desTag;
	}

	public void setDesTag(String desTag) {
		this.desTag = desTag;
	}

	public String getRoomTag() {
		return roomTag;
	}

	public void setRoomTag(String roomTag) {
		this.roomTag = roomTag;
	}

	public String getAddressTag() {
		return addressTag;
	}

	public void setAddressTag(String addressTag) {
		this.addressTag = addressTag;
	}

	public String getBuyCostTag() {
		return buyCostTag;
	}

	public void setBuyCostTag(String buyCostTag) {
		this.buyCostTag = buyCostTag;
	}

	public String getRentCostTag() {
		return rentCostTag;
	}

	public void setRentCostTag(String rentCostTag) {
		this.rentCostTag = rentCostTag;
	}
}

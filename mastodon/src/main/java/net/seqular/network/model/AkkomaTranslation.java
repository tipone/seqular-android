package net.seqular.network.model;

public class AkkomaTranslation extends BaseModel{
	public String text;
	public String detectedLanguage;

	public Translation toTranslation() {
		Translation translation=new Translation();
		translation.content=text;
		translation.detectedSourceLanguage=detectedLanguage;
		translation.provider="Akkoma";
		return translation;
	}
}

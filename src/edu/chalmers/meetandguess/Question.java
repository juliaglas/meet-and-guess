package edu.chalmers.meetandguess;

import android.os.Parcel;
import android.os.Parcelable;

public class Question implements Parcelable {

	private String question;
	private String answer1;
	private String answer2;
	
	public Question(String question, String answer1, String answer2) {
		super();
		this.question = question;
		this.answer1 = answer1;
		this.answer2 = answer2;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getAnswer1() {
		return answer1;
	}

	public void setAnswer1(String answer1) {
		this.answer1 = answer1;
	}

	public String getAnswer2() {
		return answer2;
	}

	public void setAnswer2(String answer2) {
		this.answer2 = answer2;
	}

	@Override
	public int describeContents() {
		return hashCode();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(question);
		dest.writeString(answer1);
		dest.writeString(answer2);
	}
	
	public Question(Parcel p) {
		question = p.readString();
		answer1 = p.readString();
		answer2 = p.readString();
	}

	public static final Parcelable.Creator<Question> CREATOR = new Parcelable.Creator<Question>() {
		@Override
		public Question createFromParcel(Parcel parcel) {
			return new Question(parcel);
		}
		@Override
		public Question[] newArray(int size) {
			return new Question[size];
		}
	};

	@Override
	public String toString() {
		return "Question [question=" + question + ", answer1=" + answer1
				+ ", answer2=" + answer2 + "]";
	}

}

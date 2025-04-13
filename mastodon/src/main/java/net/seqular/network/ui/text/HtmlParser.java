package net.seqular.network.ui.text;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.BulletSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TypefaceSpan;
import android.text.style.UnderlineSpan;
import android.widget.TextView;

import com.twitter.twittertext.Regex;

import net.seqular.network.R;
import net.seqular.network.model.Emoji;
import net.seqular.network.model.FilterResult;
import net.seqular.network.model.Hashtag;
import net.seqular.network.model.Mention;
import net.seqular.network.ui.utils.UiUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.jsoup.select.NodeVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;

import me.grishka.appkit.utils.V;

public class HtmlParser{
	private static final String TAG="HtmlParser";
	private static final String VALID_URL_PATTERN_STRING =
					"(" +                                                            //  $1 total match
						"(" + Regex.URL_VALID_PRECEDING_CHARS + ")" +                        //  $2 Preceding character
						"(" +                                                          //  $3 URL
						"(https?://)" +                                             //  $4 Protocol (optional)
						"(" + Regex.URL_VALID_DOMAIN + ")" +                               //  $5 Domain(s)
						"(?::(" + Regex.URL_VALID_PORT_NUMBER + "))?" +                    //  $6 Port number (optional)
						"(/" +
						Regex.URL_VALID_PATH + "*+" +
						")?" +                                                       //  $7 URL Path and anchor
						"(\\?" + Regex.URL_VALID_URL_QUERY_CHARS + "*" +                   //  $8 Query String
						Regex.URL_VALID_URL_QUERY_ENDING_CHARS + ")?" +
						")" +
					")";
	public static final Pattern URL_PATTERN=Pattern.compile(VALID_URL_PATTERN_STRING, Pattern.CASE_INSENSITIVE);
	public static final Pattern INVITE_LINK_PATTERN=Pattern.compile("^https://"+Regex.URL_VALID_DOMAIN+"/invite/[a-z\\d]+$", Pattern.CASE_INSENSITIVE);
	private static Pattern EMOJI_CODE_PATTERN=Pattern.compile(":([\\w]+):");

	private HtmlParser(){}

	public static SpannableStringBuilder parse(String source, List<Emoji> emojis, List<Mention> mentions, List<Hashtag> tags, String accountID){
		return parse(source, emojis, mentions, tags, accountID, null, null);
	}

	public static SpannableStringBuilder parse(String source, List<Emoji> emojis, List<Mention> mentions, List<Hashtag> tags, String accountID, Context context){
		return parse(source, emojis, mentions, tags, accountID, null, context);
	}

	public static SpannableStringBuilder parse(String source, List<Emoji> emojis, List<Mention> mentions, List<Hashtag> tags, String accountID, Object parentObject){
		return parse(source, emojis, mentions, tags, accountID, parentObject, null);
	}

	/**
	 * Parse HTML and custom emoji into a spanned string for display.
	 * Supported tags: <ul>
	 * <li>&lt;a class="hashtag | mention | (none)"></li>
	 * <li>&lt;span class="invisible | ellipsis"></li>
	 * <li>&lt;br/></li>
	 * <li>&lt;p></li>
	 * </ul>
	 * @param source Source HTML
	 * @param emojis Custom emojis that are present in source as <code>:code:</code>
	 * @return a spanned string
	 */
	public static SpannableStringBuilder parse(String source, List<Emoji> emojis, List<Mention> mentions, List<Hashtag> tags, String accountID, Object parentObject, Context context){
		class SpanInfo{
			public Object span;
			public int start;
			public Element element;
			public boolean more;

			public SpanInfo(Object span, int start, Element element){
				this(span, start, element, false);
			}

			public SpanInfo(Object span, int start, Element element, boolean more){
				this.span=span;
				this.start=start;
				this.element=element;
				this.more=more;
			}
		}

		Map<String, String> idsByUrl=mentions.stream().filter(mention -> mention.id != null).collect(Collectors.toMap(m->m.url, m->m.id));
		// Hashtags in remote posts have remote URLs, these have local URLs so they don't match.
//		Map<String, String> tagsByUrl=tags.stream().collect(Collectors.toMap(t->t.url, t->t.name));
		Map<String, Hashtag> tagsByTag=tags.stream().distinct().collect(Collectors.toMap(t->t.name.toLowerCase(), Function.identity()));

		final SpannableStringBuilder ssb=new SpannableStringBuilder();
		int colorInsert=UiUtils.getThemeColor(context, R.attr.colorM3Success);
		int colorDelete=UiUtils.getThemeColor(context, R.attr.colorM3Error);

		if(source.endsWith("\n"))
			source=source.stripTrailing();

		Jsoup.parseBodyFragment(source).body().traverse(new NodeVisitor(){
			private final ArrayList<SpanInfo> openSpans=new ArrayList<>();

			@Override
			public void head(@NonNull Node node, int depth){
				if(node instanceof TextNode textNode){
					ssb.append(textNode.text());
				}else if(node instanceof Element el){
					switch(el.nodeName()){
						case "a" -> {
							Object linkObject=null;
							String href=el.attr("href");
							LinkSpan.Type linkType;
							String text=el.text();
							if(!TextUtils.isEmpty(text) && (el.hasClass("hashtag") || text.startsWith("#"))){
								// MOSHIDON: we have slightly refactored this so that the hashtags properly work in akkoma
								// TODO: upstream this
								linkType=LinkSpan.Type.HASHTAG;
								href=text.substring(1);
								linkObject=tagsByTag.get(text.substring(1).toLowerCase());
							}else if(el.hasClass("mention")){
								String id=idsByUrl.get(href);
								if(id!=null){
									linkType=LinkSpan.Type.MENTION;
									href=id;
								}else{
									linkType=LinkSpan.Type.URL;
								}
							}else{
								linkType=LinkSpan.Type.URL;
							}
							openSpans.add(new SpanInfo(new LinkSpan(href, null, linkType, accountID, linkObject, parentObject, text), ssb.length(), el));
						}
						case "br" -> ssb.append('\n');
						case "span" -> {
							if(el.hasClass("invisible")){
								openSpans.add(new SpanInfo(new InvisibleSpan(), ssb.length(), el));
							}
						}
						case "li" -> openSpans.add(new SpanInfo(new BulletSpan(V.dp(8)), ssb.length(), el));
						case "em", "i" -> openSpans.add(new SpanInfo(new StyleSpan(Typeface.ITALIC), ssb.length(), el));
						case "h1", "h2", "h3", "h4", "h5", "h6" -> {
							// increase line height above heading (multiplying the margin)
							if (node.previousSibling()!=null) ssb.setSpan(new RelativeSizeSpan(2), ssb.length() - 1, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
							if (!node.nodeName().equals("h1")) {
								openSpans.add(new SpanInfo(new StyleSpan(Typeface.BOLD), ssb.length(), el));
							}
							openSpans.add(new SpanInfo(new RelativeSizeSpan(switch(node.nodeName()) {
								case "h1" -> 1.5f;
								case "h2" -> 1.25f;
								case "h3" -> 1.125f;
								default -> 1;
							}), ssb.length(), el, !node.nodeName().equals("h1")));
						}
						case "strong", "b" -> openSpans.add(new SpanInfo(new StyleSpan(Typeface.BOLD), ssb.length(), el));
						case "u" -> openSpans.add(new SpanInfo(new UnderlineSpan(), ssb.length(), el));
						case "s", "del" -> openSpans.add(new SpanInfo(new StrikethroughSpan(), ssb.length(), el));
						case "sub", "sup" -> {
							openSpans.add(new SpanInfo(node.nodeName().equals("sub") ? new SubscriptSpan() : new SuperscriptSpan(), ssb.length(), el));
							openSpans.add(new SpanInfo(new RelativeSizeSpan(0.8f), ssb.length(), el, true));
						}
						case "code", "pre" -> openSpans.add(new SpanInfo(new TypefaceSpan("monospace"), ssb.length(), el));
						case "blockquote" -> openSpans.add(new SpanInfo(new LeadingMarginSpan.Standard(V.dp(10)), ssb.length(), el));
						// fake elements for the edit history diff view
						case "edit-diff-insert" -> openSpans.add(new SpanInfo(new ForegroundColorSpan(colorInsert), ssb.length(), el));
						case "edit-diff-delete" -> openSpans.add(new SpanInfo(new DiffRemovedSpan(el.text(), colorDelete), ssb.length(), el));
					}
				}
			}

			final static List<String> blockElements = Arrays.asList("p", "ul", "ol", "blockquote", "h1", "h2", "h3", "h4", "h5", "h6");

			@Override
			public void tail(@NonNull Node node, int depth){
				if(node instanceof Element el){
					processOpenSpan(el);
					if("span".equals(el.nodeName()) && el.hasClass("ellipsis")){
						ssb.append("…", new DeleteWhenCopiedSpan(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					}else if(blockElements.contains(el.nodeName()) && node.nextSibling()!=null){
						ssb.append("\n"); // line end
						ssb.append("\n", new RelativeSizeSpan(0.65f), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // margin after block
					}
				}
			}

			private void processOpenSpan(Element el) {
				if(!openSpans.isEmpty()){
					SpanInfo si=openSpans.get(openSpans.size()-1);
					if(si.element==el){
						ssb.setSpan(si.span, si.start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
						openSpans.remove(openSpans.size()-1);
						if(si.more) processOpenSpan(el);
					}
					if("li".equals(el.nodeName()) && el.nextSibling()!=null) {
						ssb.append('\n');
					}
				}
			}
		});
		if(!emojis.isEmpty())
			parseCustomEmoji(ssb, emojis);
		return ssb;
	}

	public static void parseCustomEmoji(SpannableStringBuilder ssb, List<Emoji> emojis){
		if(emojis==null) return;
		Map<String, Emoji> emojiByCode =
			emojis.stream()
			.collect(
				Collectors.toMap(e->e.shortcode, Function.identity(), (emoji1, emoji2) -> {
					// Ignore duplicate shortcodes and just take the first, it will be
					// the same emoji anyway
					return emoji1;
				})
			);

		Matcher matcher=EMOJI_CODE_PATTERN.matcher(ssb);
		int spanCount=0;
		CustomEmojiSpan lastSpan=null;
		while(matcher.find()){
			Emoji emoji=emojiByCode.get(matcher.group(1));
			if(emoji==null)
				continue;
			ssb.setSpan(lastSpan=new CustomEmojiSpan(emoji), matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			spanCount++;
		}
		if(spanCount==1 && ssb.getSpanStart(lastSpan)==0 && ssb.getSpanEnd(lastSpan)==ssb.length()){
			ssb.append(' '); // To fix line height
		}
	}

	public static SpannableStringBuilder parseCustomEmoji(String text, List<Emoji> emojis){
		SpannableStringBuilder ssb=new SpannableStringBuilder(text);
		parseCustomEmoji(ssb, emojis);
		return ssb;
	}

	public static void setTextWithCustomEmoji(TextView view, String text, List<Emoji> emojis){
		if(!EMOJI_CODE_PATTERN.matcher(text).find()){
			view.setText(text);
			return;
		}
		view.setText(parseCustomEmoji(text, emojis));
		UiUtils.loadCustomEmojiInTextView(view);
	}

	public static String strip(String html){
		return Jsoup.clean(html, Safelist.none());
	}

	public static String stripAndRemoveInvisibleSpans(String html){
		Document doc=Jsoup.parseBodyFragment(html);
		doc.body().select("span.invisible").remove();
		Cleaner cleaner=new Cleaner(Safelist.none().addTags("br", "p"));
		StringBuilder sb=new StringBuilder();
		cleaner.clean(doc).body().traverse(new NodeVisitor(){
			@Override
			public void head(Node node, int depth){
				if(node instanceof TextNode tn){
					sb.append(tn.text());
				}else if(node instanceof Element el){
					if("br".equals(el.tagName())){
						sb.append('\n');
					}
				}
			}

			@Override
			public void tail(Node node, int depth){
				if(node instanceof Element el && "p".equals(el.tagName()) && el.nextSibling()!=null){
					sb.append("\n\n");
				}
			}
		});
		return sb.toString();
	}

	public static String text(String html) {
		return Jsoup.parse(html).body().wholeText();
	}

	public static CharSequence parseLinks(String text){
		Matcher matcher=URL_PATTERN.matcher(text);
		if(!matcher.find()) // Return the original string if there are no URLs
			return text;
		SpannableStringBuilder ssb=new SpannableStringBuilder(text);
		do{
			String url=matcher.group(3);
			if(TextUtils.isEmpty(matcher.group(4)))
				url="http://"+url;
			ssb.setSpan(new LinkSpan(url, null, LinkSpan.Type.URL, null, null, null, url), matcher.start(3), matcher.end(3), 0);
		}while(matcher.find()); // Find more URLs
		return ssb;
	}

	public static void applyFilterHighlights(Context context, SpannableStringBuilder text, List<FilterResult> filters){
		int fgColor=UiUtils.getThemeColor(context, R.attr.colorM3Error);
		int bgColor=UiUtils.getThemeColor(context, R.attr.colorM3ErrorContainer);
		for(FilterResult filter:filters){
			if(!filter.filter.isActive())
				continue;
			for(String word:filter.keywordMatches){
				Matcher matcher=Pattern.compile("\\b"+Pattern.quote(word)+"\\b", Pattern.CASE_INSENSITIVE).matcher(text);
				while(matcher.find()){
					ForegroundColorSpan fg=new ForegroundColorSpan(fgColor);
					BackgroundColorSpan bg=new BackgroundColorSpan(bgColor);
					text.setSpan(bg, matcher.start(), matcher.end(), 0);
					text.setSpan(fg, matcher.start(), matcher.end(), 0);
				}
			}
		}
	}
}

package org.keycloak.sms.auth;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @title 验证码工具类
 * @author boat
 */
public class ImageCodeUtil {

	private static int width = 90;// 定义图片的width
	private static int height = 30;// 定义图片的height

	// 5个验证码一组
	/*
	 * private static int codeCount = 5;// 定义图片上显示验证码的个数 private static int xx = 13;
	 */

	// 4个验证码一组
	private static int codeCount = 4;// 定义图片上显示验证码的个数
	private static int xx = 15;

	private static int codeY = 23;
	private static int fontHeight = 22;

	private static char[] codeSequence = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
			'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
			'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4',
			'5', '6', '7', '8', '9' };

	/**
	 * 生成一个map集合 code为生成的验证码 codePic为生成的验证码BufferedImage对象
	 *
	 * @return
	 * @throws IOException
	 */
	public static Map<String, String> generateCodeAndPic() throws IOException {
		// 定义图像buffer
		BufferedImage buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		// Graphics2D gd = buffImg.createGraphics();
		// Graphics2D gd = (Graphics2D) buffImg.getGraphics();
		Graphics gd = buffImg.getGraphics();

		// 创建一个随机数生成器类
		Random random = new Random();

		// 将图像填充为白色
		gd.setColor(Color.WHITE);
		gd.fillRect(0, 0, width, height);

		// 创建字体，字体的大小应该根据图片的高度来定。
		Font font = new Font("Arial", Font.BOLD, fontHeight);

		// 设置字体。
		gd.setFont(font);

		// 画边框。
		// gd.setColor(Color.LIGHT_GRAY);
		gd.setColor(getRandomColor());
		gd.drawRect(0, 0, width - 1, height - 1);

		// 随机产生40条干扰线，使图象中的认证码不易被其它程序探测到。
		gd.setColor(getRandomColor());

		for (int i = 0; i < 30; i++) {
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			int xl = random.nextInt(12);
			int yl = random.nextInt(12);
			gd.drawLine(x, y, x + xl, y + yl);
		}

		// randomCode用于保存随机产生的验证码，以便用户登录后进行验证。
		StringBuffer randomCode = new StringBuffer();

		// 随机产生codeCount数字的验证码。
		for (int i = 0; i < codeCount; i++) {

			// 得到随机产生的验证码数字。
			String code = String.valueOf(codeSequence[random.nextInt(codeSequence.length)]);

			// 用随机产生的颜色将验证码绘制到图像中。
			gd.setColor(getRandomColor());

			gd.drawString(code, (i + 1) * xx, codeY);

			// 将产生的四个随机数组合在一起。
			randomCode.append(code);
		}
		Map<String, String> map = new HashMap<String, String>();
		// 存放验证码
		map.put("code", randomCode.toString());
		// 存放生成的验证码BufferedImage对象
		// 创建编码对象
		Base64.Encoder base64 = Base64.getEncoder();
		// 创建字符流
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		// 写入字符流
		ImageIO.write(buffImg, "jpg", bs);
		// 转码成字符串
		String imgsrc = base64.encodeToString(bs.toByteArray());

		map.put("codePic", imgsrc);
		return map;
	}

	/**
	 * 随机取色
	 */
	public static Color getRandomColor() {
		Random ran = new Random();
		int red = ran.nextInt(256);
		int green = ran.nextInt(256);
		int blue = ran.nextInt(256);
		Color color = new Color(red, green, blue);
		return color;
	}

	// 获取验证码
	public static Map<String, String> getCheckCodePic() throws IOException {
		return ImageCodeUtil.generateCodeAndPic();
	}
}

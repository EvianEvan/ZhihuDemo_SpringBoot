package com.nowcoder.service;

// 视频 5 重点：关键代码：问题提交按钮功能 - 敏感词过滤功能：
// (问题提交功能的主体代码在 QuestionService.java)
// 利用 前缀树/字典树 实现 提交问题时的敏感词过滤功能；

// 若直接搜索敏感词替换(即从字符串中从头往后搜，看每个字符串是否为字符串集中某个字符串)，
// 则时间复杂度为 O(N2)；若使用前缀树则为 O(N);

// 前缀树：
// • 根节点不包含字符，除根节点外每一个节点都只包含一个字符；
// • 从根节点到某一节点，路径上经过的字符连接起来，为该节点对应的字符串；
// • 每个节点的所有子节点(注意只是直系子节点不是孙节点)包含的字符都不相同；

// Trie树，即字典树，又称单词查找树或键树，是一种树形结构，是一种哈希树的变种。
// Trie这个术语来自于 retrieval，发音为/tri:/ “tree”，也有人读为/traɪ/ “try”。
// 典型应用是用于统计和排序大量的字符串（但不仅限于字符串），所以经常被搜索引擎系统用于文本词频统计。
// 它的优点是：最大限度地减少无谓的字符串比较。
// Trie的核心思想是空间换时间。
// 利用字符串的公共前缀来降低查询时间的开销以达到提高效率的目的。

// 假设字符的种数有m个，有若干个长度为n的字符串构成了一个Trie树，
// 则每个节点的出度为m（即每个节点的可能子节点数量为m），
// Trie树的高度为n。很明显我们浪费了大量的空间来存储字符，
// 此时Trie树的最坏空间复杂度为O(m^n)。
// 也正由于每个节点的出度为m，
// 所以我们能够沿着树的一个个分支高效的向下逐个字符的查询，而不是遍历所有的字符串来查询，
// 此时Trie树的最坏时间复杂度为O(n)。
// 这正是空间换时间的体现，也是利用公共前缀降低查询时间开销的体现。

// 举个在网上流传颇广的例子，如下：
// 题目：给你 100000个长度不超过10的单词。
// 对于每一个单词，我们要判断他出没出现过，如果出现了，求第一次出现在第几个位置。
//
// 分析：这题当然可以用hash来解决，但是本文重点介绍的是trie树，
// 因为在某些方面它的用途更大。
// 比如说对于某一个单词，我们要询问它的前缀是否出现过。
// 这样hash就不好搞了，而用trie还是很简单。
//
// 现在回到例子中，如果我们用最傻的方法，对于每一个单词，我们都要去查找它前面的单词中是否有它。
// 那么这个算法的复杂度就是O(n^2)。显然对于100000的范围难以接受。
// 现在我们换个思路想。假设我要查询的单词是abcd，那么在他前面的单词中，以b，c，d，f之类开头的我显然不必考虑。
// 而只要找以a开头的中是否存在abcd就可以了。
// 同样的，在以a开头中的单词中，我们只要考虑以b作为第二个字母的，
// 一次次缩小范围和提高针对性，这样一个树的模型就渐渐清晰了。

// 利用Trie实现敏感词过滤器主要有下面三个步骤：
// 1. 定义前缀树
// 2. 根据敏感词，初始化前缀树
// 3. 编写过滤敏感词的算法

// 具体讲解见：推荐：https://blog.csdn.net/weixin_41927235/article/details/102975797 ；

import org.apache.commons.lang.CharUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
// 通过重写 InitializingBean 中的 afterPropertiesSet方法
// 来实现对敏感词列表 main/resources/SensitiveWords.txt的读取加载：
public class SensitiveService implements InitializingBean {
  private static final Logger logger = LoggerFactory.getLogger(SensitiveService.class);

  //
  // 函数1：定义前缀树的数据结构：
  private class TrieNode {

    // 是不是敏感词的结尾：
    private boolean end = false;
    // 关键：保存当前节点下所有的直系子节点：
    // 不知道以这个节点开头后面会有几个，所以用 map表示某节点的直系子节点：
    // 树其实就是用这个变量来构造的，一直延伸下去：
    private Map<Character, TrieNode> subNodes = new HashMap<Character, TrieNode>();

    // 函数1：增加当前节点的直系子节点：
    void addSubNode(Character key, TrieNode node) {
      subNodes.put(key, node);
    }

    // 函数2：获取当前节点的直系子节点：
    TrieNode getSubNode(Character key) {
      return subNodes.get(key);
    }

    // 函数3：判断是否是一个敏感词的结尾：
    boolean isKeyWordEnd() {
      return end;
    }

    // 函数4: 设置当前节点为一个敏感词的结尾（初始化时）：
    void setKeyWordEnd(boolean end) {
      this.end = end;
    }
  }

  private TrieNode rootNode = new TrieNode();

  //
  // 函数2：建立前缀树（即增加敏感词：将敏感词一个一个地加进来）：
  // 例如 敏感词lineTxt为 abc：
  private void addWord(String lineTxt) {
    // 一开始指向树的根节点：
    TrieNode tempNode = rootNode;
    // 然后开始遍历词（abc）：
    for (int i = 0; i < lineTxt.length(); i++) {
      Character c = lineTxt.charAt(i);

      // 增强版过滤功能：
      // 如果输入的敏感词列表中也有特殊字符的话，
      // 可以在建树时就把特殊字符忽略掉（如为 赌@@博 建树时只为 赌博 建立节点）；
      if (isSymbol(c)) {
        continue;
      }

      //  先看根节点下有没有为 a的直系子节点：
      TrieNode node = tempNode.getSubNode(c);
      // 如果没有a的话, 则在根节点下面挂一个a：
      if (node == null) {
        node = new TrieNode();
        tempNode.addSubNode(c, node);
      }
      // 如果有a或者已挂上a的话，直接将当前节点指向下一个节点：
      tempNode = node;
      // 在敏感词的最后一个节点将结尾标记设为 true：
      if (i == lineTxt.length() - 1) {
        tempNode.setKeyWordEnd(true);
      }
    }
  }

  //  //
  //  // 函数3：重要：核心函数：实现敏感词过滤功能：
  //  // 初版：
  //  public String filter(String text) {
  //    if (text.trim() == "") {
  //      return text;
  //    }
  //
  //    String replacement = "***";
  //    StringBuilder result = new StringBuilder();
  //
  //    // 定义三个指针：分别对应视频中的指针1、2、3：
  //    TrieNode treePoint = rootNode; // 前缀树中的指针；
  //    int slowPoint = 0; // 基准指针：被动移动，只会前移，不会向后回移；
  //    int fastPoint = 0; // 比较指针：每次都与前缀树指针的直系子节点比较，看有无相同字符；
  //
  //    while (fastPoint < text.length()) {
  //      char c = text.charAt(fastPoint);
  //      // 移动时每次都是 fastPoint和 treePoint进行比较，看 treePoint的直系子节点中有没有包含相同字符的节点；
  //      treePoint = treePoint.getSubNode(c);
  //      // 如果没有相同字符的节点：
  //      if (treePoint == null) {
  //        result.append(text.charAt(slowPoint));
  //        slowPoint++;
  //        fastPoint = slowPoint;
  //        treePoint = rootNode;
  //        // 如果有相同字符的节点，且为敏感词的结尾节点：
  //      } else if (treePoint.isKeyWordEnd()) {
  //        result.append(replacement);
  //        fastPoint++;
  //        slowPoint = fastPoint;
  //        treePoint = rootNode;
  //        // 如果有相同字符的节点，但不是敏感词的结尾节点，则继续往后走：
  //      } else {
  //        fastPoint++;
  //      }
  //    }
  ////   处理特殊情况：若敏感词长度大于某部分处理文本的长度，则需要把最后一次未处理的字符加上；
  ////   如把此句注释掉则测试函数输出为：
  ////   你好***啊
  ////   A
  ////   如不把此句注释掉则测试函数输出为：
  ////   你好***啊
  ////   Aabcd
  //      result.append(text.substring(slowPoint));
  //      return result.toString();
  //    }

  //
  // 函数3：重要：核心函数：实现敏感词过滤功能：
  // 增强版：对夹杂特殊符号的敏感词也能识别（特殊符号包括空格和其它符号）：
  // （PS：如果输入的敏感词列表中也有特殊字符的话，在建树（addWord）的时候可以把特殊符号过滤掉）；
  //
  // 添加一个识别符号的函数：
  private boolean isSymbol(char c) {
    int ic = (int) c;
    // 判断既不是字母、数字也不是东亚文字（东亚文字：0x2E80 - 0x9FFF），即为特殊符号；
    return !CharUtils.isAsciiAlphanumeric(c) && (ic < 0x2E80 || ic > 0x9FFF);
  }

  public String filter(String text) {
    if (text.trim().equals("")) {
      return text;
    }
    String replacement = "***";
    StringBuilder result = new StringBuilder();

    // 定义三个指针：分别对应视频中的指针1、2、3：
    TrieNode treePoint = rootNode; // 前缀树中的指针；
    int slowPoint = 0; // 基准指针：被动移动，只会前移，不会向后回移；
    int fastPoint = 0; // 比较指针：每次都与前缀树指针的直系子节点比较，看有无相同字符；

    while (fastPoint < text.length()) {

      char c = text.charAt(fastPoint);

      // 加强部分: 若为符号则直接跳过；
      if (isSymbol(c)) {
        // 在每次处理时，若开始处就是特殊符号则保存到结果，避免丢失；
        // （以保证只跳过敏感词中间的特殊符号，而不跳过敏感词之外的特殊符号）
        if (treePoint == rootNode) {
          result.append(c);
          slowPoint++;
        }
        fastPoint++;
        continue;
      }
      // 加强部分: END；

      // 移动时每次都是 fastPoint和 treePoint进行比较，看 treePoint的直系子节点中有没有包含相同字符的节点；
      treePoint = treePoint.getSubNode(c);
      // 如果没有相同字符的节点：
      if (treePoint == null) {
        result.append(text.charAt(slowPoint));
        slowPoint++;
        fastPoint = slowPoint;
        treePoint = rootNode;
        // 如果有相同字符的节点，且为敏感词的结尾节点：
      } else if (treePoint.isKeyWordEnd()) {
        result.append(replacement);
        fastPoint++;
        slowPoint = fastPoint;
        treePoint = rootNode;
        // 如果有相同字符的节点，但不是敏感词的结尾节点，则继续往后走：
      } else {
        fastPoint++;
      }
    }

    // 处理特殊情况：若敏感词（abcdef）长度大于某部分处理文本（Aabcd中的abcd）的长度，则需要把最后一次未处理的字符加上；
    // 如把此句注释掉则测试函数输出为：
    // 你好***啊
    // A
    // 如不把此句注释掉则测试函数输出为：
    // 你好***啊
    // Aabcd
    result.append(text.substring(slowPoint));
    return result.toString();
  }

  //
  // 函数4：测试函数（可以直接只运行本文件，写代码时方便调试）：
  public static void main(String[] args) {
    SensitiveService s = new SensitiveService();
    // 测试用例 1：
    s.addWord("赌博");
    s.addWord("色情");
    System.out.println(s.filter("你好色情啊"));
    // 测试用例 2：
    s.addWord("abcdef");
    System.out.println(s.filter("Aabcd"));
    // 测试用例 2：
    System.out.println(s.filter("你好色 情啊"));
    System.out.println(s.filter("hi 你好色---情啊"));
    System.out.println(s.filter("🙂🙂 你好🙂@色🙂🙂情🙂@@啊"));

    // 测试输出：
    // 你好***啊
    // Aabcd
    // 你好***啊
    // hi 你好***啊
    // 🙂🙂 你好🙂@***🙂@@啊
  }

  //
  // 函数5：主体运行函数：
  // 利用 service类的初始化过程，为项目读取敏感词列表（main/resources/SensitiveWords.txt），建立敏感词前缀树：
  @Override
  public void afterPropertiesSet() throws Exception {
    try {
      // 读取文件：（也可用其它方法读取）
      InputStream is =
          Thread.currentThread().getContextClassLoader().getResourceAsStream("SensitiveWords.txt");
      // 逐行读取文件：
      InputStreamReader reader = new InputStreamReader(is);
      BufferedReader bufferedReader = new BufferedReader(reader);
      String lineTxt;
      while ((lineTxt = bufferedReader.readLine()) != null) {
        addWord(lineTxt.trim());
      }
      reader.close();
    } catch (Exception e) {
      logger.error("读取敏感词文件失败: " + e.getMessage());
    }
  }
}

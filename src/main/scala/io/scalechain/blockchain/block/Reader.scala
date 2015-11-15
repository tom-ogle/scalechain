package io.scalechain.blockchain.block

import java.io.{BufferedInputStream, DataInputStream, File, FileInputStream}

import io.scalechain.blockchain.script.ScriptParser
import io.scalechain.util.HexUtil

/**
 * Created by kangmo on 2015. 11. 1..
 */

case class Timestamp(val unixTimestamp : Int)

abstract class Hash(private val hash : Array[Byte])
{
  def isAllZero() = {
    // BUGBUG : Dirty code. make it cleaner!
    var countOfZero = 0
    for ( byteValue : Byte <- hash ) {
      if (byteValue == 0)
        countOfZero += 1
    }
    (countOfZero == hash.length)
  }

  def toHex() : String = {
    s"${HexUtil.prettyHex(hash.reverse)}"
  }
}

case class BlockHash(val hash : Array[Byte]) extends Hash(hash) {
  override def toString() : String = {
    s"BlockHash(size:${hash.length}, ${HexUtil.prettyHex(hash)})"
  }
}
case class MerkleRootHash(val hash : Array[Byte]) extends Hash(hash) {
  override def toString() : String = {
    s"MerkleRootHash(size:${hash.length}, ${HexUtil.prettyHex(hash)})"
  }
}

case class TransactionHash(val hash : Array[Byte]) extends Hash(hash) {
  override def toString() : String = {
    s"TransactionHash(size:${hash.length}, ${HexUtil.prettyHex(hash)})"
  }
}




case class BlockHeader(val version : Int, hashPrevBlock : BlockHash, hashMerkleRoot : MerkleRootHash, time : Timestamp, target : Int, nonce : Int) {
  override def toString() : String = {
    s"BlockHeader(version:$version, $hashPrevBlock, $hashMerkleRoot, $time, target:$target, nonce:$nonce)"
  }
}

case class CoinbaseData(data: Array[Byte]) {
  override def toString() : String = {
    s"CoinbaseData(size:${data.length}, ${HexUtil.prettyHex(data)})"
  }
}



class TransactionInput(val transactionHash : TransactionHash)

case class NormalTransactionInput(override val transactionHash : TransactionHash, outputIndex : Int, unlockingScript : UnlockingScript, sequenceNumber : Int) extends TransactionInput(transactionHash) {
  override def toString(): String = {
    s"NormalTransactionInput($transactionHash, outputIndex:$outputIndex, $unlockingScript, sequenceNumber:$sequenceNumber)"
  }
}

case class GenerationTransactionInput(override val transactionHash : TransactionHash,
                                      outputIndex : Int,
                                      coinbaseData : CoinbaseData,
                                      sequenceNumber : Int) extends TransactionInput(transactionHash) {
  override def toString(): String = {
    s"GenerationTransactionInput($transactionHash, outputIndex:$outputIndex, $coinbaseData, sequenceNumber:$sequenceNumber)"
  }
}



abstract class Script(private val data:Array[Byte])

case class LockingScript(val data:Array[Byte]) extends Script(data) {
/*
  // --- For debugging +++
  println( s"LockingScript(size:${data.length}, ${HexUtil.prettyHex(data)})" )
  val scriptOps = ScriptParser.parse(data)
  println( s"LockingScript(size:${data.length}, $scriptOps)" )
  // --- For debugging +++
*/

  override def toString(): String = {
    val scriptOps = ScriptParser.parse(data)
//    s"LockingScript(size:${data.length}, ${HexUtil.prettyHex(data)})"
    s"LockingScript(size:${data.length}, $scriptOps)"
  }
}

case class UnlockingScript(val data:Array[Byte]) extends Script(data) {
/*
  // --- For debugging +++
  println( s"UnlockingScript(size:${data.length}, ${HexUtil.prettyHex(data)})" )
  val scriptOps = ScriptParser.parse(data)
  println( s"UnlockingScript(size:${data.length}, $scriptOps)" )
  // --- For debugging +++
*/
  override def toString(): String = {
    val scriptOps = ScriptParser.parse(data)
//    s"UnlockingScript(size:${data.length}, ${HexUtil.prettyHex(data)})"
    s"UnlockingScript(size:${data.length}, $scriptOps)"
  }
}


case class TransactionOutput(value : Long, lockingScript : LockingScript) {
  override def toString(): String = {
    s"TransactionOutput(value : $value, $lockingScript)"
  }
}

case class Transaction(version : Int,
                       inputs : Array[TransactionInput],
                       outputs : Array[TransactionOutput],
                       lockTime : Int) {

  override def toString() : String = {
    // TODO : HashCalculator depends on Transaction, and also Transaction depends on HashCalculator. Get rid of the circular dependency.
    s"Transaction(version:$version, [${inputs.mkString(",")}], [${outputs.mkString(",")}], lockTime:$lockTime, _hash:${HashCalculator.transactionHash(this)}})"
  }


  /** Calculate hash value for a given transaction input, and part of script that unlocks the UTXO attached to the input.
   * Why use part of script instead of all script bytes?
   *
   * 1. We need to use bytes after the OP_CODESEPARATOR in the script.
   * 2. We need to get rid of all signature data from the script.
   * 3. We need to get rid of OP_CODESEPARATOR OP code from the script.
   *
   * @param transactionInputIndex The index of the transaction input to get the hash.
   * @param scriptData A part of unlocking script for the UTXO attached to the given transaction input.
   * @param howToHash Decides how to calculate the hash value from this transaction and the given script.
   *                  The value should be one of values in Transaction.SigHash
   * @return The calculated hash value.
   */
  def hashForSignature(transactionInputIndex : Int, scriptData : Array[Byte], howToHash : Int) : Array[Byte] = {
    // TODO : Implement
    assert(false);
    null
  }
}

object Transaction {
  object SigHash {
    val ALL = 1
    val NONE = 2
    val SINGLE = 3
  }
  val ANYONECANPAY : Int = 0x80
}

case class Block(val size:Long,
                 val header:BlockHeader,
                 val transactions : Array[Transaction]) {


  override def toString() : String = {
    s"Block(size:$size, $header, [${transactions.mkString(",")}])"
  }
}

/** For each block read by the block reader, we will have a function call.
 *
 */
trait BlockReadListener {
  /** This function is called whenever we finish reading and decoding a block.
   *
   * @param block The block read by the Reader.
   */
  def onBlock(block : Block ): Unit
}




/** Read a block file such as blk00000.dat and produce list of blocks
 *
  * @param blockListener We will call onBlock function of the listener for each block we read.
 */
class BlockFileReader(val blockListener : BlockReadListener) {
  /** Read all blocks in the file. Call onBlock for each block we read.
   *
    * @param blockFile the file to read.
   */
  def readFully(blockFile : File): Unit = {
    var stream : BlockDataInputStream= null
    try {
      stream = new BlockDataInputStream( new DataInputStream( new BufferedInputStream (new FileInputStream(blockFile))) );
      while( readBlock(stream) ) {
        // do nothing
      }
    } finally {
      stream.close();
    }
  }

  /**
   * Read a block from the input stream.
   * @param stream The byte array stream where we read the block data.
   * @return True if a block was read, False if we met EOF of the input block file.
   */
  def readBlock(stream : BlockDataInputStream): Boolean = {
    val parser = new BlockParser(stream)
    val blockOption = parser.parse();
    if (blockOption.isDefined) {
      blockListener.onBlock(blockOption.get)
      true
    } else {
      false
    }
  }
}


/** Read the blockchain data downloaded by the reference Bitcoin core implementation.
 *
 */
class BlockDirectoryReader(val blockListener : BlockReadListener) {

  /** Reads list of blocks written in Blockchain.
    * This function starts reading blk00000.dat and produced blocks.
    * After finishing reading the file, it continues to read blk00001.dat, and so on.
    *
    * @param path The path that has blkNNNNN.dat files.
    * @return
    */
  def readFrom(path : String) {
    val directory = new File(path);
    // For each file in the path
    for (file <- directory.listFiles.sortBy(_.getName())
         if (file.getName().startsWith("blk") && file.getName().endsWith(".dat")) ) {
      val fileReader = new  BlockFileReader(blockListener);
      fileReader.readFully(file)
    }
  }
}

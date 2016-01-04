package io.scalechain.blockchain.block.index

import io.scalechain.blockchain.block.Block
import io.scalechain.blockchain.block.{Transaction, TransactionHash, BlockHash, Block}

/**
 * Created by kangmo on 11/16/15.
 */
trait BlockIndex {
  /** Get a block by its hash.
   *
   * @param blockHash
   */
  def getBlock(blockHash : BlockHash) : Option[Block]

  /** Get a transaction by its hash.
   *
   * @param transactionHash
   */
  def getTransaction(transactionHash : TransactionHash) : Option[Transaction]
}

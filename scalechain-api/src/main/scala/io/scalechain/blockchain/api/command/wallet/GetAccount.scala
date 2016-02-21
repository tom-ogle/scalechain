package io.scalechain.blockchain.api.command.wallet

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

/*
  CLI command :
    bitcoin-cli -testnet getaccount mjSk1Ny9spzU2fouzYgLqGUD8U41iR35QN

  CLI output :
    doc test

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "getaccount", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

case class GetAccountResult(
) extends RpcResult


/** GetAccount: returns the name of the account associated with the given address.
  *
  * https://bitcoin.org/en/developer-reference#getaccount
  */
object GetAccount extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, RpcResult] = {
    // TODO : Implement
    assert(false)
    Right(null)
  }
  def help() : String =
    """getaccount "bitcoinaddress"
      |
      |DEPRECATED. Returns the account associated with the given address.
      |
      |Arguments:
      |1. "bitcoinaddress"  (string, required) The bitcoin address for account lookup.
      |
      |Result:
      |"accountname"        (string) the account address
      |
      |Examples:
      |> bitcoin-cli getaccount "1D1ZrZNe3JUo7ZycKEYQQiQAWd9y54F4XZ"
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getaccount", "params": ["1D1ZrZNe3JUo7ZycKEYQQiQAWd9y54F4XZ"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}


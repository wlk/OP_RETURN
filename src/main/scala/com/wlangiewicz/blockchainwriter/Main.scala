package com.wlangiewicz.blockchainwriter

import java.io.File
import java.math.BigInteger

import org.bitcoinj.core._
import org.bitcoinj.net.discovery.DnsDiscovery
import org.bitcoinj.params.{MainNetParams, RegTestParams, TestNet3Params}
import org.bitcoinj.script.{ScriptOpCodes, ScriptBuilder}
import org.bitcoinj.store.{MemoryBlockStore, SPVBlockStore}


object Main extends App{
  if (args.length < 2) {
    Console.err.println("Usage: private-key regtest|testnet|mainnet")
  }
  else{
    Console.println("Creating wallet")
    var filePrefix: String = null

    // Figure out which network we should connect to. Each one gets its own set of files.
    val params: NetworkParameters = args(1) match {
      case "mainnet" => {
        filePrefix = "forwarding-service-testnet"
        MainNetParams.get
      }
      case "testnet" => {
        filePrefix = "forwarding-service-testnet"
        TestNet3Params.get
      }
      case "regtest" => {
        filePrefix = "forwarding-service-regtest"
        RegTestParams.get
      }
    }

    // Parse the address given as the first parameter.
    val privateKey = addressToKey(params, args(0))

    //val chainStore: SPVBlockStore = new SPVBlockStore(params, new File(filePrefix + ".spvchain"))
    val chainStore: MemoryBlockStore = new MemoryBlockStore(params)
    val chain: BlockChain = new BlockChain(params, chainStore)

    val wallet = new Wallet(params)
    wallet.importKey(privateKey)

    val peers: PeerGroup = new PeerGroup(params, chain)
    peers.addPeerDiscovery(new DnsDiscovery(params))

    chain.addWallet(wallet)
    peers.addWallet(wallet)

    peers.startAsync
    peers.awaitRunning()
    peers.downloadBlockChain()

    Console.println(wallet.toString)

    val tx: Transaction = new Transaction(params)
    //val messagePrice = Coin.parseCoin("0.001")
    //val messagePrice = Transaction.MIN_NONDUST_OUTPUT
    val messagePrice = Coin.ZERO
    val script = new ScriptBuilder().op(ScriptOpCodes.OP_RETURN).data("hello".getBytes).build()

    tx.addOutput(messagePrice, script) //0 BTC goes to
    tx.addOutput(wallet.getBalance, new Address(params, "mx6EmoJHfEuNZFnhKcd2fBML3ri9PscP2M"))


    val request: Wallet.SendRequest  = Wallet.SendRequest.forTx(tx)
    wallet.completeTx(request)
    Console.println("inputs: " + tx.getInputs)
    Console.println("outputs: " + tx.getOutputs)
    //wallet.commitTx(request.tx)

    peers.broadcastTransaction(request.tx).get()

    wallet.addEventListener(new AbstractWalletEventListener {
      override def onCoinsSent(w: Wallet, tx: Transaction, prevBalance: Coin, newBalance: Coin): Unit ={
        Console.println("transaction: " + tx)
      }
    })

    peers.stopAsync
    peers.awaitTerminated()
  }

  def addressToKey(params: NetworkParameters, sourceAddress: String): ECKey = {
    sourceAddress match {
      case _ if sourceAddress.length == 51 || sourceAddress.length == 52 => new DumpedPrivateKey(params, sourceAddress).getKey //WIF
      case _ if sourceAddress.length == 64 => ECKey.fromPrivate(new BigInteger(sourceAddress, 16)) //hex encoded private key
      case _ => ECKey.fromPrivate(Base58.decodeToBigInteger(sourceAddress)) //base58
    }
  }
}
